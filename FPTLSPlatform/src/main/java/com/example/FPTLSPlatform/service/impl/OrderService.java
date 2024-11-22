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
                LocalDateTime cancelDeadline = scheduledClass.getStartDate().atTime(orderDetail.getClasses().getSlot().getStartTime()).minusMinutes(checkTime);

                if (now.isAfter(cancelDeadline)) {
                    return new ResponseDTO<>("ERROR", "Cannot cancel the order within " + checkTime + " minutes before the class starts.", null);
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


    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void checkAndActivateClasses() {
        int pageNumber = 0;
        int defaultTime = 1;

        // Lấy giá trị cấu hình từ hệ thống
        System checkTimeBeforeStart = systemRepository.findByName("check_time_before_start");
        int checkTime = checkTimeBeforeStart != null
                ? Integer.parseInt(checkTimeBeforeStart.getValue())
                : defaultTime;

        System demoMode = systemRepository.findByName("demo_mode");
        boolean isDemoMode = demoMode != null && Boolean.parseBoolean(demoMode.getValue());

        System demoAdjustTime = systemRepository.findByName("demo_adjust_time");
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
                    // Tính toán thời gian bắt đầu lớp
                    LocalDateTime classStartTime = scheduledClass.getStartDate()
                            .atTime(scheduledClass.getSlot().getStartTime())
                            .minusMinutes(checkTime);

                    // Điều chỉnh thời gian nếu demo_mode được bật
                    if (isDemoMode) {
                        classStartTime = classStartTime.minusMinutes(demoTimeAdjustment);
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
            log.info("Class with ID {} has been activated successfully.", scheduledClass.getClassId());
        } else {
            scheduledClass.setStatus(ClassStatus.CANCELED);
            classRepository.save(scheduledClass);
            notificationService.createNotification(buildNotificationDTO("Your class " + scheduledClass.getCode() + " has been cancelled",
                    "Your class " + scheduledClass.getCode() + " has been cancelled",
                    scheduledClass.getTeacher().getTeacherName()));
            log.info("Class with ID {} has been cancelled.", scheduledClass.getClassId());
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
                startTime = startTime.minusMinutes(adjustStartTime);
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

        List<Class> classesToComplete = classRepository.findByStartDateAndStatus(now.toLocalDate(), ClassStatus.ONGOING);

        for (Class scheduledClass : classesToComplete) {
            LocalDateTime endTime = scheduledClass.getStartDate().atTime(scheduledClass.getSlot().getEndTime());

            if (isDemoMode) {
                endTime = endTime.minusMinutes(adjustEndTime);
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
                SystemWallet systemWallet = systemWalletRepository.getReferenceById(1L);
                systemWallet.setTotalAmount(systemWallet.getTotalAmount() - scheduledClass.getPrice());
                systemWalletRepository.save(systemWallet);

                Wallet wallet = scheduledClass.getTeacher().getWallet();
                wallet.setBalance(wallet.getBalance() + scheduledClass.getPrice());
                walletRepository.save(wallet);
                TransactionHistory transactionHistory = saveTransactionHistory(scheduledClass.getTeacher().getEmail(), scheduledClass.getPrice(), wallet);
                transactionHistory.setNote("Salary");

                log.info("Class with ID {} has started and is now COMPLETED.", scheduledClass.getClassId());
            }
        }
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

}
