package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.*;
import com.example.FPTLSPlatform.exception.InsufficientBalanceException;
import com.example.FPTLSPlatform.exception.OrderAlreadyExistsException;
import com.example.FPTLSPlatform.exception.ResourceNotFoundException;
import com.example.FPTLSPlatform.model.Class;
import com.example.FPTLSPlatform.model.System;
import com.example.FPTLSPlatform.model.*;
import com.example.FPTLSPlatform.model.enums.ClassStatus;
import com.example.FPTLSPlatform.model.enums.OrderStatus;
import com.example.FPTLSPlatform.repository.*;
import com.example.FPTLSPlatform.service.IEmailService;
import com.example.FPTLSPlatform.service.INotificationService;
import com.example.FPTLSPlatform.service.IOrderService;
import com.example.FPTLSPlatform.service.IWalletService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;

    private final ClassRepository classRepository;

    private final OrderDetailRepository orderDetailRepository;

    private final UserRepository userRepository;

    private final IWalletService walletService;

    private final IEmailService emailService;

    private final INotificationService notificationService;

    private final TransactionHistoryRepository transactionHistoryRepository;

    private final SystemWalletRepository systemWalletRepository;

    private final ClassService classService;

    private final SystemRepository systemRepository;

//    private final ClassStatusController classStatusController;

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final WalletRepository walletRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        ClassRepository classRepository,
                        OrderDetailRepository orderDetailRepository,
                        UserRepository userRepository,
                        IWalletService walletService,
                        IEmailService emailService,
                        INotificationService notificationService,
                        TransactionHistoryRepository transactionHistoryRepository,
                        SystemWalletRepository systemWalletRepository,
                        ClassService classService,
                        SystemRepository systemRepository,
//                        ClassStatusController classStatusController
                        WalletRepository walletRepository) {
        this.orderRepository = orderRepository;
        this.classRepository = classRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.userRepository = userRepository;
        this.walletService = walletService;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.systemWalletRepository = systemWalletRepository;

        this.classService = classService;
        this.systemRepository = systemRepository;
//        this.classStatusController = classStatusController;
        this.walletRepository = walletRepository;
    }

    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(order -> new OrderDTO(order.getOrderId(), order.getUser().getUserName(), order.getCreateAt(), order.getTotalPrice(), order.getStatus()));
    }

    @Override
    public ListTotalOrderDTO getTotalOrders(LocalDateTime startDate, LocalDateTime endDate) {
        TotalOrderDTO totalOrderDTO = orderRepository.getTotalOrdersAndAmountByDateRange(startDate, endDate);
        List<OrderDetail> orderDetails = orderRepository.getOrderDetailsByDateRange(startDate, endDate);
        ListTotalOrderDTO listTotalOrderDTO = new ListTotalOrderDTO();
        listTotalOrderDTO.setTotalOrderDTO(totalOrderDTO);
        listTotalOrderDTO.setOrderDetails(orderDetails.stream().map(orderDetail -> OrderDetailDTO.builder()
                .orderDetailId(orderDetail.getOrderDetailId())
                .orderDTO(OrderDTO.builder()
                        .orderId(orderDetail.getOrder().getOrderId())
                        .totalPrice(orderDetail.getOrder().getTotalPrice())
                        .username(orderDetail.getOrder().getUser().getUserName())
                        .createAt(orderDetail.getOrder().getCreateAt())
                        .status(orderDetail.getOrder().getStatus())
                        .build())
                .classDTO(classService.mapEntityToDTO(orderDetail.getClasses()))
                .price(orderDetail.getPrice())
                .build()).toList());
        return listTotalOrderDTO;
    }

    @Override
    public OrderDTO createOrder(Long classId, String username) throws Exception {
        Class scheduleClass = getClassOrThrow(classId);
        User user = getUserOrThrow(username);

        OrderDetail existingOrderDetail = orderDetailRepository.findByOrder_User_UserNameAndClasses_ClassId(username, classId);
        Order order;

        checkOrderAlreadyExists(username, classId);

        Wallet wallet = walletService.getWalletByUserName();
        checkSufficientBalance(wallet, scheduleClass.getPrice());
        checkClassCapacity(classId, scheduleClass.getMaxStudents());

        if (existingOrderDetail != null && existingOrderDetail.getOrder().getStatus().equals(OrderStatus.CANCELLED)) {
            order = existingOrderDetail.getOrder();
            order.setStatus(OrderStatus.PENDING);
            order.setCreateAt(LocalDateTime.now());
        } else {
            checkOrderAlreadyExists(username, classId);

            order = new Order();
            order.setUser(user);
            order.setCreateAt(LocalDateTime.now());
            order.setStatus(OrderStatus.PENDING);
            order.setTotalPrice(scheduleClass.getPrice());
            order = orderRepository.save(order);

            saveOrderDetail(order, scheduleClass);
        }

        wallet.setBalance(wallet.getBalance() - scheduleClass.getPrice());
        TransactionHistory transactionHistory = saveTransactionHistory(user.getEmail(), -order.getTotalPrice(), wallet);
        transactionHistory.setNote("Order");
        userRepository.save(wallet.getUser());

        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("class", scheduleClass);
        context.setVariable("teacherName", scheduleClass.getTeacher().getTeacherName());
        emailService.sendEmail(order.getUser().getEmail(), "Booking successful", "order-email", context);

        // Tạo thông báo
        notificationService.createNotification(NotificationDTO.builder()
                .title("Class " + scheduleClass.getName() + " has been booked.")
                .description("Class " + scheduleClass.getName() + " has been successfully booked. Your new balance " + formatToVND(wallet.getBalance()) + "(-" + formatToVND(order.getTotalPrice()) + ")")
                .username(order.getUser().getUserName())
                .type("Create Order")
                .name("Order Notification")
                .build());

        return OrderDTO.builder()
                .orderId(order.getOrderId())
                .username(order.getUser().getUserName())
                .createAt(order.getCreateAt())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .build();
    }

    @Transactional
    public ResponseDTO<String> cancelOrder(Long orderId) {
        try {
            Order order = orderRepository.findById(String.valueOf(orderId))
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

            // Check if the order is already cancelled
            if (Objects.equals(order.getStatus(), OrderStatus.CANCELLED)) {
                return new ResponseDTO<>("ERROR", "Cannot cancel the order because the order has already been cancelled.", null);
            }

            OrderDetail orderDetail = orderDetailRepository.findByOrder_OrderId(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order detail not found for order id: " + orderId));

            Class scheduledClass = orderDetail.getClasses();


            if (Objects.equals(order.getStatus(), OrderStatus.PENDING)) {
                int defaultTime = 1;
                System checkTimeBeforeStart = systemRepository.findByName("check_time_before_start");
                int checkTime = checkTimeBeforeStart != null
                        ? Integer.parseInt(checkTimeBeforeStart.getValue())
                        : defaultTime;
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime cancelDeadline = scheduledClass.getStartDate().atTime(orderDetail.getClasses().getSlot().getStartTime()).minusDays(checkTime);

                if (now.isAfter(cancelDeadline)) {
                    return new ResponseDTO<>("ERROR", "Cannot cancel the order within " + checkTime + " days before the class starts.", null);
                }

                // Refund and update order status
                walletService.refundToWallet(order.getTotalPrice());
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
                Wallet wallet = walletService.getWalletByUserName();
                Context context = new Context();
                context.setVariable("username", order.getUser().getUserName());
                context.setVariable("class", scheduledClass);
                emailService.sendEmail(order.getUser().getEmail(), "Cancelled booking successful", "cancel-email", context);
                notificationService.createNotification(NotificationDTO.builder()
                        .title("Class " + scheduledClass.getName() + " has been cancelled")
                        .description("Class " + scheduledClass.getName() + " has been cancelled and had refunded. Your new balance " + formatToVND(wallet.getBalance()) + "(+" + formatToVND(order.getTotalPrice()) + ")")
                        .name("Notification")
                        .type("Cancel Order")
                        .username(order.getUser().getUserName())
                        .build());
                return new ResponseDTO<>("SUCCESS", "Order cancelled successfully", null);
            }

            return new ResponseDTO<>("ERROR", "Cannot cancel the order with current status.", null);

        } catch (Exception ex) {
            return new ResponseDTO<>("ERROR", ex.getMessage(), null);
        }
    }

    @Override
    public Page<OrderDTO> getOrdersByUser(String username, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserName(username, pageable);
        return orders
                .map(order -> OrderDTO.builder()
                        .orderId(order.getOrderId())
                        .username(order.getUser().getUserName())
                        .createAt(order.getCreateAt())
                        .totalPrice(order.getTotalPrice())
                        .status(order.getStatus())
                        .build());
    }


    public Page<OrderDetailDTO> getClassesOrderedByUser(String username, Pageable pageable) {
        Page<OrderDetail> orderDetails = orderDetailRepository.findByOrder_User_UserName(username, pageable);

        return orderDetails.map(orderDetail -> OrderDetailDTO.builder()
                .orderDetailId(orderDetail.getOrderDetailId())
                .orderDTO(OrderDTO.builder()
                        .orderId(orderDetail.getOrder().getOrderId())
                        .totalPrice(orderDetail.getOrder().getTotalPrice())
                        .username(orderDetail.getOrder().getUser().getUserName())
                        .createAt(orderDetail.getOrder().getCreateAt())
                        .status(orderDetail.getOrder().getStatus())
                        .build())
                .classDTO(classService.mapEntityToDTO(orderDetail.getClasses()))
                .price(orderDetail.getPrice())
                .build());
    }


    @Scheduled(cron = "0 0 * * * *") // Chạy mỗi giờ
    public void sendUpcomingClassReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime upcomingThreshold = now.plusHours(24);

        List<Class> upcomingClasses = classRepository.findByStatusAndStartDateBetween(ClassStatus.ACTIVE, now.toLocalDate(), upcomingThreshold.toLocalDate());

        for (Class upcomingClass : upcomingClasses) {
            LocalTime startTime = upcomingClass.getSlot().getStartTime();
            LocalTime endTime = upcomingClass.getSlot().getEndTime();

            Page<OrderDetail> orderDetails = orderDetailRepository.findByClasses_ClassId(upcomingClass.getClassId(), Pageable.unpaged());
            for (OrderDetail orderDetail : orderDetails) {
                notificationService.createNotification(NotificationDTO.builder()
                        .title("Reminder: Upcoming Class " + upcomingClass.getCode())
                        .description("Your class " + upcomingClass.getName() + " will start on " +
                                upcomingClass.getStartDate() + " from " + startTime + " to " + endTime)
                        .name("Notification")
                        .type("Class Reminder")
                        .username(orderDetail.getOrder().getUser().getUserName())
                        .build());
            }

            notificationService.createNotification(NotificationDTO.builder()
                    .title("Reminder: You have an upcoming class")
                    .description("Class " + upcomingClass.getName() + " (Code: " + upcomingClass.getCode() + ") will start on " +
                            upcomingClass.getStartDate() + " from " + startTime + " to " + endTime)
                    .name("Notification")
                    .type("Class Reminder")
                    .username(upcomingClass.getTeacher().getTeacherName())
                    .build());
        }
    }

    @Override
    public void refundStudents(Class cancelledClass) {
        Page<OrderDetail> orderDetails = orderDetailRepository.findByClasses_ClassId(cancelledClass.getClassId(), Pageable.unpaged());

        for (OrderDetail orderDetail : orderDetails) {
            Order order = orderDetail.getOrder();
            User student = order.getUser();

            if (orderDetail.getPrice() <= 0) {
                throw new IllegalArgumentException("Refund amount cannot be less than zero.");
            }

            Wallet wallet = student.getWallet();
            wallet.setBalance(student.getWallet().getBalance() + (orderDetail.getPrice()));
            userRepository.save(student);

            TransactionHistory transactionHistory = saveTransactionHistory(student.getEmail(), orderDetail.getPrice(), wallet);
            transactionHistory.setNote("Refunded");
            notificationService.createNotification(NotificationDTO.builder()
                    .title("Refund for Order " + order.getOrderId() + " has been processed")
                    .description("Your class " + cancelledClass.getName() + " has been canceled, and a refund has been initiated. Your new balance " + formatToVND(wallet.getBalance()) + "( + " + formatToVND(order.getTotalPrice()) + ")")
                    .name("Notification")
                    .type("Refund Notification")
                    .username(order.getUser().getUserName())
                    .build());

        }
    }

    private void checkOrderAlreadyExists(String username, Long classId) {
        OrderDetail orderDetail = orderDetailRepository.findByOrder_User_UserNameAndClasses_ClassId(username, classId);
        if (orderDetail != null && !OrderStatus.CANCELLED.equals(orderDetail.getOrder().getStatus())) {
            throw new OrderAlreadyExistsException("User has already registered for this class.");
        }
        if (hasDuplicateSchedule(username, classId)) {
            throw new IllegalStateException("User has already registered for this schedule.");
        }
    }

    private void saveOrderDetail(Order order, Class scheduledClass) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrder(order);
        orderDetail.setClasses(scheduledClass);
        orderDetail.setPrice(scheduledClass.getPrice());
        orderDetailRepository.save(orderDetail);
    }

    public boolean hasDuplicateSchedule(String username, Long classId) {
        Page<OrderDetail> userOrders = orderDetailRepository.findByOrder_User_UserName(username, Pageable.unpaged());

        Class newClass = classRepository.getReferenceById(classId);

        for (OrderDetail orderDetail : userOrders) {
            if (orderDetail.getOrder().getStatus().equals(OrderStatus.CANCELLED)) {
                continue;
            }

            Class existingClass = orderDetail.getClasses();
            if (existingClass != null
                    && existingClass.getDayOfWeek().equals(newClass.getDayOfWeek())
                    && existingClass.getSlot().equals(newClass.getSlot())) {
                return true;
            }
        }
        return false;
    }

    private TransactionHistory saveTransactionHistory(String email, double amount, Wallet wallet) {
        TransactionHistory transactionHistory = new TransactionHistory();
        transactionHistory.setAmount(amount);
        transactionHistory.setTransactionDate(LocalDateTime.now());
        transactionHistory.setUser(wallet.getUser());
        transactionHistory.setTeacher(wallet.getTeacherName());
        transactionHistory.setTransactionBalance(wallet.getBalance());

        transactionHistoryRepository.save(transactionHistory);
        Context context = new Context();
        context.setVariable("transactionHistory", transactionHistory);
        context.setVariable("teacherName", wallet.getTeacherName());
        emailService.sendEmail(email, "Transaction", "transaction-email", context);

        return transactionHistory;
    }

    private Class getClassOrThrow(Long id) {
        return classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));
    }

    private User getUserOrThrow(String username) {
        return userRepository.findById(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    private void checkSufficientBalance(Wallet wallet, double requiredAmount) {
        if (wallet.getBalance() < requiredAmount) {
            throw new InsufficientBalanceException("Wallet balance is not enough to register for class.");
        }
    }

    private void checkClassCapacity(Long classId, int maxStudents) throws Exception {
        long activeRegistrations = orderDetailRepository.countByClasses_ClassIdAndOrder_StatusNot(classId, OrderStatus.CANCELLED);

        if (activeRegistrations >= maxStudents) {
            throw new Exception("Class is fully booked.");
        }
    }

    @Override
    public void sendActivationEmail(Class scheduledClass) {
        try {
            Context context = new Context();
            context.setVariable("teacherName", scheduledClass.getTeacher().getTeacherName());
            context.setVariable("class", scheduledClass);
            emailService.sendEmail(scheduledClass.getTeacher().getEmail(), "Class active", "active-email", context);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    @Override
    public void sendCancelEmail(Class scheduledClass) {
        try {
            Context context = new Context();
            context.setVariable("teacherName", scheduledClass.getTeacher().getTeacherName());
            context.setVariable("class", scheduledClass);
            emailService.sendEmail(scheduledClass.getTeacher().getEmail(), "Class cancel", "cancel-email", context);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void checkAndActivateClasses() {
        int pageNumber = 0;
        int defaultTime = 1;

        System checkTimeBeforeStart = systemRepository.findByName("check_time_before_start");
        int checkTime = checkTimeBeforeStart != null
                ? Integer.parseInt(checkTimeBeforeStart.getValue())
                : defaultTime;

        System demoMode = systemRepository.findByName("demo_mode");
        boolean isDemoMode = demoMode != null && Boolean.parseBoolean(demoMode.getValue());

        System demoAdjustTime = systemRepository.findByName("demo_adjust_active_time");
        int demoTimeAdjustment = demoAdjustTime != null
                ? Integer.parseInt(demoAdjustTime.getValue())
                : 0;

        while (true) {
            Pageable pageable = PageRequest.of(pageNumber, 50);
            Page<Class> classesPage = classRepository.findByStatusAndStartDate(ClassStatus.PENDING, LocalDate.now(), pageable);

            if (classesPage.isEmpty()) {
                break;
            }

            for (Class scheduledClass : classesPage) {
                try {
                    LocalDateTime classStartTime = scheduledClass.getStartDate()
                            .atTime(scheduledClass.getSlot().getStartTime())
                            .minusDays(checkTime);

                    if (isDemoMode) {
                        classStartTime = classStartTime.minusDays(demoTimeAdjustment);
                    }

                    if (classStartTime.isBefore(LocalDateTime.now())) {
                        Class clazz = activateClassIfEligible(scheduledClass);
                        Page<OrderDetail> orderDetails = orderDetailRepository.findByClasses_ClassId(clazz.getClassId(), Pageable.unpaged());
                        handleOrderDetails(orderDetails, scheduledClass);
                    }
                } catch (Exception e) {
                    log.error("Unexpected error occurred while activating class {}: {}", scheduledClass.getClassId(), e.getMessage());
                }
            }
            pageNumber++;
        }
    }

    @Transactional
    protected Class activateClassIfEligible(Class scheduledClass) {
        int registeredStudents = orderDetailRepository.countByClasses_ClassIdAndOrder_StatusNot(scheduledClass.getClassId(), OrderStatus.CANCELLED);
        double minimumPercentage = getMinimumPercentage();
        int minimumRequiredStudents = (int) Math.ceil(scheduledClass.getMaxStudents() * minimumPercentage);
        log.info("register:{}, minimum: {}", registeredStudents, minimumRequiredStudents);

        if (registeredStudents >= minimumRequiredStudents) {
            scheduledClass.setStatus(ClassStatus.ACTIVE);
            classRepository.save(scheduledClass);
            notificationService.createNotification(buildNotificationDTO("Your class " + scheduledClass.getCode() + " has been activated",
                    "Class " + scheduledClass.getCode() + " is starting on " + scheduledClass.getStartDate(),
                    scheduledClass.getTeacher().getTeacherName()));

            sendActivationEmail(scheduledClass);
        } else {
            scheduledClass.setStatus(ClassStatus.CANCELED);
            classRepository.save(scheduledClass);
            notificationService.createNotification(buildNotificationDTO("Your class " + scheduledClass.getCode() + " has been cancelled",
                    "Your class " + scheduledClass.getCode() + " has been cancelled",
                    scheduledClass.getTeacher().getTeacherName()));
            sendCancelEmail(scheduledClass);
        }
        return scheduledClass;
    }

    @Transactional
    protected void handleOrderDetails(Page<OrderDetail> orderDetails, Class scheduledClass) {
        List<OrderDetail> updatedOrderDetails = new ArrayList<>();
        for (OrderDetail orderDetail : orderDetails) {
            if (scheduledClass.getStatus().equals(ClassStatus.ACTIVE)) {
                orderDetail.getOrder().setStatus(OrderStatus.ACTIVE);
            } else {
                orderDetail.getOrder().setStatus(OrderStatus.CANCELLED);
                refundStudents(scheduledClass);
            }
            updatedOrderDetails.add(orderDetail);

            // Send Notification to Users
            notificationService.createNotification(NotificationDTO.builder()
                    .title("Class " + scheduledClass.getCode() + " has been " + (scheduledClass.getStatus().equals(ClassStatus.ACTIVE) ? "activated" : "cancelled"))
                    .description("Class " + scheduledClass.getCode() + " has been " + (scheduledClass.getStatus().equals(ClassStatus.ACTIVE) ? "activated" : "cancelled"))
                    .name("Notification")
                    .type(scheduledClass.getStatus().equals(ClassStatus.ACTIVE) ? "Active Order" : "Cancelled Order")
                    .username(orderDetail.getOrder().getUser().getUserName())
                    .build());
        }
        orderDetailRepository.saveAll(updatedOrderDetails); // Bulk save the order details
    }

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void updateClassesToOngoing() {
        LocalDateTime now = LocalDateTime.now();

        // Lấy cấu hình demo_mode
        System demoMode = systemRepository.findByName("demo_mode");
        boolean isDemoMode = demoMode != null && Boolean.parseBoolean(demoMode.getValue());

        System demoAdjustStartTime = systemRepository.findByName("demo_adjust_start_time");
        int adjustStartTime = demoAdjustStartTime != null ? Integer.parseInt(demoAdjustStartTime.getValue()) : 0;

        List<Class> classesToStart = classRepository.findByStartDateAndStatus(now.toLocalDate(), ClassStatus.ACTIVE);

        for (Class scheduledClass : classesToStart) {
            LocalDateTime startTime = scheduledClass.getStartDate().atTime(scheduledClass.getSlot().getStartTime());

            if (isDemoMode) {
                startTime = startTime.minusDays(adjustStartTime);
            }

            if (now.isAfter(startTime) && scheduledClass.getStatus().equals(ClassStatus.ACTIVE)) {
                scheduledClass.setStatus(ClassStatus.ONGOING);
                classRepository.save(scheduledClass);
                Page<OrderDetail> orderDetails = orderDetailRepository.findByClasses_ClassId(scheduledClass.getClassId(), Pageable.unpaged());
                for (OrderDetail orderDetail : orderDetails) {
                    if (orderDetail.getOrder().getStatus().equals(OrderStatus.ACTIVE)) {
                        orderDetail.getOrder().setStatus(OrderStatus.ONGOING);
                        orderDetailRepository.save(orderDetail);
                    }
                }
                log.info("Class with ID {} has started and is now ONGOING.", scheduledClass.getClassId());
            }
        }
    }


    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void checkAndCompleteOrders() {
        LocalDateTime now = LocalDateTime.now();

        System demoMode = systemRepository.findByName("demo_mode");
        boolean isDemoMode = demoMode != null && Boolean.parseBoolean(demoMode.getValue());

        System demoAdjustEndTime = systemRepository.findByName("demo_adjust_end_time");
        int adjustEndTime = demoAdjustEndTime != null ? Integer.parseInt(demoAdjustEndTime.getValue()) : 0;
        System discountPercentage = systemRepository.findByName("discount_percentage");
        double discount = discountPercentage != null ? Double.parseDouble(discountPercentage.getValue()) : 0;
        List<Class> classesToComplete = classRepository.findByStartDateAndStatus(now.toLocalDate(), ClassStatus.ONGOING);

        for (Class scheduledClass : classesToComplete) {
            LocalDateTime endTime = scheduledClass.getStartDate().atTime(scheduledClass.getSlot().getEndTime());

            if (isDemoMode) {
                endTime = endTime.minusDays(adjustEndTime);
            }

            if (now.isAfter(endTime)) {
                scheduledClass.setStatus(ClassStatus.COMPLETED);
                classRepository.save(scheduledClass);
                Page<OrderDetail> orderDetails = orderDetailRepository.findByClasses_ClassId(scheduledClass.getClassId(), Pageable.unpaged());
                for (OrderDetail orderDetail : orderDetails) {
                    if (orderDetail.getOrder().getStatus().equals(OrderStatus.ONGOING)) {
                        orderDetail.getOrder().setStatus(OrderStatus.COMPLETED);
                        orderDetailRepository.save(orderDetail);
                    }
                }
                saveSystemWallet(discount, scheduledClass);

                log.info("Class with ID {} has started and is now COMPLETED.", scheduledClass.getClassId());
            }
        }
    }

    private void saveSystemWallet(double discount, Class scheduledClass) {
        SystemWallet systemWallet = systemWalletRepository.getReferenceById(1L);
        List<StudentDTO> studentDTOS = classRepository.findStudentsByClassId(scheduledClass.getClassId());
        double totalAmount = (scheduledClass.getPrice() * discount) * studentDTOS.size();
        Wallet wallet = scheduledClass.getTeacher().getWallet();
        TransactionHistory transactionHistory = saveTransactionHistory(scheduledClass.getTeacher().getEmail(), totalAmount, wallet);
        transactionHistory.setNote("Salary");
        SystemTransactionHistory systemTransactionHistory = saveSystemTransactionHistory(totalAmount, systemWallet);
        systemTransactionHistory.setNote("Salary");
        systemTransactionHistory.setUsername(scheduledClass.getTeacher().getTeacherName());

    }

    private SystemTransactionHistory saveSystemTransactionHistory(double totalAmount, SystemWallet systemWallet) {
        SystemTransactionHistory systemTransactionHistory = new SystemTransactionHistory();
        systemTransactionHistory.setTransactionAmount(-totalAmount);
        systemTransactionHistory.setTransactionDate(LocalDateTime.now());
        systemTransactionHistory.setBalanceAfterTransaction(systemWallet.getTotalAmount());
        systemWallet.setTotalAmount(systemWallet.getTotalAmount() - totalAmount);
        systemWalletRepository.save(systemWallet);

        return systemTransactionHistory;
    }


    private double getMinimumPercentage() {
        System minimumPercentageParam = systemRepository.findByName("minimum_required_percentage");
        return minimumPercentageParam != null
                ? Double.parseDouble(minimumPercentageParam.getValue())
                : 0.8;
    }

    private NotificationDTO buildNotificationDTO(String title, String description, String username) {
        return NotificationDTO.builder()
                .title(title)
                .description(description)
                .type("Active Class Notification")
                .username(username)
                .name("Notification")
                .build();
    }

    public static String formatToVND(double amount) {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(Math.abs(amount)) + " VND";
    }

    @Transactional
    public void completeClassImmediately(Long classId) {
        // Tìm lớp học theo ID
        Class scheduledClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        // Cập nhật trạng thái lớp học thành COMPLETED
        scheduledClass.setStatus(ClassStatus.COMPLETED);
        classRepository.save(scheduledClass);
        System discountPercentage = systemRepository.findByName("discount_percentage");
        double discount = discountPercentage != null ? Double.parseDouble(discountPercentage.getValue()) : 0;
        // Lấy tất cả các OrderDetail liên quan đến lớp
        Page<OrderDetail> orderDetails = orderDetailRepository.findByClasses_ClassId(classId, Pageable.unpaged());

        // Cập nhật trạng thái đơn hàng liên quan
        for (OrderDetail orderDetail : orderDetails) {
            orderDetail.getOrder().setStatus(OrderStatus.COMPLETED);
            orderDetailRepository.save(orderDetail);
        }

        // Xử lý thanh toán: trừ tiền từ SystemWallet và thêm vào ví của giáo viên
        saveSystemWallet(discount, scheduledClass);
        // Gửi thông báo cho giáo viên
        notificationService.createNotification(NotificationDTO.builder()
                .title("Class " + scheduledClass.getCode() + " has been completed")
                .name("Notification")
                .description("Your class " + scheduledClass.getCode() + " has been successfully completed.")
                .type("Class Completed")
                .username(scheduledClass.getTeacher().getTeacherName())
                .build());

        // Log kết quả
        log.info("Class with ID {} has been completed immediately by admin.", classId);
    }


    @Transactional
    public String cancelClass(Long classId) {
        Class classToCancel = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found with ID: " + classId));

        if (classToCancel.getStatus() == ClassStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel a class that is already completed.");
        } else if (classToCancel.getStatus() == ClassStatus.CANCELED) {
            throw new RuntimeException("This class has already been canceled.");
        }

        List<OrderDetail> orderDetails = orderDetailRepository.findByClasses_ClassId(classId);

        for (OrderDetail orderDetail : orderDetails) {
            Order order = orderDetail.getOrder();
            User student = order.getUser();

            Wallet studentWallet = student.getWallet();
            if (studentWallet == null) {
                throw new RuntimeException("Student does not have a wallet for refund.");
            }
            studentWallet.setBalance(studentWallet.getBalance() + orderDetail.getPrice());
            walletRepository.save(studentWallet);

            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

        }
        notificationService.createNotification(buildNotificationDTO("Your class " + classToCancel.getCode() + " has been cancelled",
                "Your class " + classToCancel.getCode() + " has been cancelled",
                classToCancel.getTeacher().getTeacherName()));


        classToCancel.setStatus(ClassStatus.CANCELED);
        classRepository.save(classToCancel);
        sendCancelEmail(classToCancel);
        return "Class with ID " + classId + " has been successfully canceled, and refunds have been processed.";
    }

}
