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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    private final WalletRepository walletRepository;

    private final SystemRepository systemRepository;

//    private final ClassStatusController classStatusController;

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

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
                        WalletRepository walletRepository,
                        SystemRepository systemRepository
//                        ClassStatusController classStatusController
    ) {
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
        this.walletRepository = walletRepository;
        this.systemRepository = systemRepository;
//        this.classStatusController = classStatusController;
    }

    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(order -> new OrderDTO(order.getOrderId(), order.getUser().getUserName(), order.getCreateAt(), order.getTotalPrice(), order.getStatus()));
    }

    @Override
    public TotalOrderDTO getTotalOrders(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.getTotalOrdersAndAmountByDateRange(startDate, endDate);
    }

    @Override
    public OrderDTO createOrder(Long classId, String username) throws Exception {
        Class scheduleClass = getClassOrThrow(classId);
        User user = getUserOrThrow(username);

        OrderDetail existingOrderDetail = orderDetailRepository.findByOrder_User_UserNameAndClasses_ClassId(username, classId);
        Order order;

        checkOrderAlreadyExists(username, classId);

        SystemWallet systemWallet = systemWalletRepository.getReferenceById(1L);
        Wallet wallet = walletService.getWalletByUserName();
        checkSufficientBalance(wallet, scheduleClass.getPrice());
        checkClassCapacity(classId, scheduleClass.getMaxStudents());

        if (existingOrderDetail != null && existingOrderDetail.getOrder().getStatus().equals(OrderStatus.CANCELLED)) {
            // Đơn hàng đã bị hủy và cần phục hồi
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
        systemWallet.setTotalAmount(systemWallet.getTotalAmount() + scheduleClass.getPrice());
        systemWalletRepository.save(systemWallet);

        // Lưu lịch sử giao dịch
        TransactionHistory transactionHistory = saveTransactionHistory(order.getUser(), -order.getTotalPrice(), wallet);
        transactionHistory.setNote("Order");
        userRepository.save(wallet.getUser());

        // Gửi email xác nhận
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("class", scheduleClass);
        context.setVariable("teacherName", scheduleClass.getTeacher().getTeacherName());
        emailService.sendEmail(order.getUser().getEmail(), "Booking successful", "order-email", context);

        // Tạo thông báo
        notificationService.createNotification(NotificationDTO.builder()
                .title("Order " + order.getOrderId() + " has been booked")
                .description("Your order " + order.getOrderId() + " has been successfully booked")
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
                int defaultDay = 0;
                System defaultDayBefore = systemRepository.findByName("day_check");
                int subtractDay = defaultDayBefore != null
                        ? Integer.parseInt(defaultDayBefore.getValue())
                        : defaultDay;
                LocalDate now = LocalDate.now();
                LocalDate cancelDeadline = scheduledClass.getStartDate().minusDays(subtractDay);

                if (now.isAfter(cancelDeadline)) {
                    return new ResponseDTO<>("ERROR", "Cannot cancel the order within 2 days before the class starts.", null);
                }

                // Refund and update order status
                walletService.refundToWallet(order.getTotalPrice());
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
                Context context = new Context();
                context.setVariable("username", order.getUser().getUserName());
                context.setVariable("class", scheduledClass);
                emailService.sendEmail(order.getUser().getEmail(), "Cancelled booking successful", "cancel-email", context);
                notificationService.createNotification(NotificationDTO.builder()
                        .title("Order " + order.getOrderId() + " has been cancelled")
                        .description("Order" + order.getOrderId() + "has been cancelled")
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

    private void sendActivationEmail(Class scheduledClass) {
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
    public void updateClassesToOngoing() {
        LocalDateTime now = LocalDateTime.now();

        List<Class> classesToStart = classRepository.findByStartDateAndStatus(now.toLocalDate(), ClassStatus.ACTIVE);

        for (Class scheduledClass : classesToStart) {
            LocalDateTime startTime = scheduledClass.getStartDate().atTime(scheduledClass.getSlot().getStartTime());

            if (now.isAfter(startTime) && scheduledClass.getStatus().equals(ClassStatus.ACTIVE)) {
                Page<OrderDetail> orderDetails = orderDetailRepository.findByClasses_ClassId(scheduledClass.getClassId(), Pageable.unpaged());
                for (OrderDetail orderDetail : orderDetails) {
                    Order order = orderDetail.getOrder();
                    if (order.getStatus().equals(OrderStatus.ACTIVE)) {
                        order.setStatus(OrderStatus.ONGOING);
                        orderRepository.save(order);
                    }
                }

                // Cập nhật trạng thái của lớp học thành ONGOING
                scheduledClass.setStatus(ClassStatus.ONGOING);
                classRepository.save(scheduledClass);
                log.info("Class with ID {} has started and is now ONGOING.", scheduledClass.getClassId());
            }
        }
    }


    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void checkAndCompleteOrders() {
        LocalDateTime now = LocalDateTime.now();

        List<Class> classesToComplete = classRepository.findByStartDateAndStatus(now.toLocalDate(), ClassStatus.ONGOING);

        for (Class scheduledClass : classesToComplete) {
            LocalDateTime endTime = scheduledClass.getStartDate().atTime(scheduledClass.getSlot().getEndTime());

            if (now.isAfter(endTime)) {
                Page<OrderDetail> orderDetails = orderDetailRepository.findByClasses_ClassId(scheduledClass.getClassId(), Pageable.unpaged());
                for (OrderDetail orderDetail : orderDetails) {
                    Order order = orderDetail.getOrder();
                    if (order.getStatus().equals(OrderStatus.ONGOING) || order.getStatus().equals(OrderStatus.ACTIVE)) {
                        order.setStatus(OrderStatus.COMPLETED);
                        orderRepository.save(order);
                        Wallet wallet = orderDetail.getClasses().getTeacher().getWallet();
                        double defaultDiscount = 0.2;
                        System discountPercentage = systemRepository.findByName("discount_percentage");
                        double discountedPrice = discountPercentage != null
                                ? orderDetail.getPrice() * (1 - Double.parseDouble(discountPercentage.getValue()))
                                : defaultDiscount;
                        wallet.setBalance(wallet.getBalance() + discountedPrice);
                        saveTransactionHistory(order.getUser(), order.getTotalPrice(), wallet);
                        walletRepository.save(wallet);
                        SystemWallet systemWallet = systemWalletRepository.getReferenceById(1L);
                        systemWallet.setTotalAmount(systemWallet.getTotalAmount() - discountedPrice);
                        notificationService.createNotification(NotificationDTO.builder()
                                .title("Order " + order.getOrderId() + " has been completed")
                                .description("Order" + order.getOrderId() + "has been completed")
                                .username(order.getUser().getUserName())
                                .name("Notification")
                                .type("Complete Order")
                                .build());
                    }
                }
                if (scheduledClass.getStatus().equals(ClassStatus.ONGOING)) {
                    scheduledClass.setStatus(ClassStatus.COMPLETED);
                }
                classRepository.save(scheduledClass);
                log.info("Class with ID {} has started and is now COMPLETED.", scheduledClass.getClassId());

            }
        }
    }

    @Scheduled(cron = "0 0 * * * *") // Chạy mỗi giờ
    public void sendUpcomingClassReminders() {
        // Lấy thời gian hiện tại và thời gian giới hạn cho lớp sắp bắt đầu (trong 24 giờ tới)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime upcomingThreshold = now.plusHours(24);

        // Lọc các lớp có trạng thái ACTIVE và bắt đầu trong khoảng 24 giờ tới
        List<Class> upcomingClasses = classRepository.findByStatusAndStartDateBetween(ClassStatus.ACTIVE, now.toLocalDate(), upcomingThreshold.toLocalDate());

        for (Class upcomingClass : upcomingClasses) {
            // Thời gian bắt đầu và kết thúc theo slot của lớp học
            LocalTime startTime = upcomingClass.getSlot().getStartTime();
            LocalTime endTime = upcomingClass.getSlot().getEndTime();

            // Gửi thông báo cho từng học viên đã đăng ký lớp
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

            // Gửi thông báo cho giáo viên phụ trách
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

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void checkAndActivateClasses() {
        int defaultDay = 0;
        System defaultDayBefore = systemRepository.findByName("day_check");
        int subtractDay = defaultDayBefore != null
                ? Integer.parseInt(defaultDayBefore.getValue())
                : defaultDay;
        LocalDateTime checkDay = LocalDateTime.now().plusDays(subtractDay);
        LocalDate dateToCheck = checkDay.toLocalDate();
        int pageNumber = 0;

        while (true) {
            Pageable pageable = PageRequest.of(pageNumber, 50);
            Page<Class> classesPage = classRepository.findByStatusAndStartDateBefore(ClassStatus.PENDING, dateToCheck, pageable);

            if (classesPage.isEmpty()) {
                break;
            }

            for (Class scheduledClass : classesPage) {
                try {
                    activateClassIfEligible(scheduledClass);
                } catch (Exception e) {
                    log.error("Unexpected error occurred while activating class {}: {}", scheduledClass.getClassId(), e.getMessage());
                }
            }
            pageNumber++;
        }
    }

    @Transactional
    protected void activateClassIfEligible(Class scheduledClass) {
        int registeredStudents = orderDetailRepository.countByClasses_ClassIdAndOrder_StatusNot(scheduledClass.getClassId(), OrderStatus.CANCELLED);
        double minimumPercentage = getMinimumPercentage();
        int minimumRequiredStudents = (int) Math.ceil(scheduledClass.getMaxStudents() * minimumPercentage);

        List<OrderDetail> orderDetails = orderDetailRepository.findByClasses_ClassId(scheduledClass.getClassId(), Pageable.unpaged()).getContent();
        if (registeredStudents >= minimumRequiredStudents) {

            orderDetails.forEach(orderDetail -> {
                Order order = orderDetail.getOrder();
                order.setStatus(OrderStatus.ACTIVE);
                notificationService.createNotification(buildNotificationDTO("Class " + scheduledClass.getCode() + " has been activated",
                        "Class " + scheduledClass.getCode() + " will start on " + scheduledClass.getStartDate() +
                                " from " + scheduledClass.getSlot().getStartTime() + " to " + scheduledClass.getSlot().getEndTime(),
                        "Active Order", order.getUser().getUserName()));
            });

            scheduledClass.setStatus(ClassStatus.ACTIVE);
            classRepository.save(scheduledClass);
            orderRepository.saveAll(orderDetails.stream().map(OrderDetail::getOrder).toList());

            notificationService.createNotification(buildNotificationDTO("Your class " + scheduledClass.getCode() + " has been activated",
                    "Class " + scheduledClass.getCode() + " is starting on " + scheduledClass.getStartDate(),
                    "Active Class Notification", scheduledClass.getTeacher().getTeacherName()));

            sendActivationEmail(scheduledClass);
            log.info("Class with ID {} has been activated successfully.", scheduledClass.getClassId());
        } else {

            orderDetails.forEach(orderDetail -> {
                Order order = orderDetail.getOrder();
                order.setStatus(OrderStatus.CANCELLED);
                notificationService.createNotification(buildNotificationDTO("Class " + scheduledClass.getCode() + " has been cancelled",
                        "Class " + scheduledClass.getCode() + "has been cancelled",
                        "Cancelled Order", order.getUser().getUserName()));
            });
            orderRepository.saveAll(orderDetails.stream().map(OrderDetail::getOrder).toList());
            scheduledClass.setStatus(ClassStatus.CANCELED);
            classRepository.save(scheduledClass);

            refundStudents(scheduledClass);
            log.info("Class with ID {} has been cancelled.", scheduledClass.getClassId());
        }
    }

    private NotificationDTO buildNotificationDTO(String title, String description, String type, String username) {
        return NotificationDTO.builder()
                .title(title)
                .description(description)
                .type(type)
                .username(username)
                .name("Notification")
                .build();
    }


    private void refundStudents(Class cancelledClass) {
        Page<OrderDetail> orderDetails = orderDetailRepository.findByClasses_ClassId(cancelledClass.getClassId(), Pageable.unpaged());

        for (OrderDetail orderDetail : orderDetails) {
            Order order = orderDetail.getOrder();
            User student = order.getUser();

            if (orderDetail.getPrice() <= 0) {
                throw new IllegalArgumentException("Refund amount cannot be less than zero.");
            }

            Wallet wallet = student.getWallet();
            SystemWallet systemWallet = systemWalletRepository.getReferenceById(1L);

            wallet.setBalance(student.getWallet().getBalance() + (orderDetail.getPrice()));
            systemWalletRepository.save(systemWallet);
            userRepository.save(student);
            TransactionHistory transactionHistory = saveTransactionHistory(wallet.getUser(), orderDetail.getPrice(), wallet);
            transactionHistory.setNote("Refunded");
            notificationService.createNotification(NotificationDTO.builder()
                    .title("Refund for Order " + order.getOrderId() + " has been processed")
                    .description("Your order with ID " + order.getOrderId() + " has been canceled, and a refund has been initiated.")
                    .name("Notification")
                    .type("Refund Notification")
                    .username(order.getUser().getUserName())
                    .build());

        }
    }

    private void checkOrderAlreadyExists(String username, Long classId) {
        OrderDetail orderDetail = orderDetailRepository.findByOrder_User_UserNameAndClasses_ClassId(username, classId);
        // Chỉ ngăn chặn nếu đơn hàng đã tồn tại và không bị hủy
        if (orderDetail != null && !orderDetail.getOrder().getStatus().equals(OrderStatus.CANCELLED)) {
            throw new OrderAlreadyExistsException("User has already registered for this class.");
        }

        // Kiểm tra trùng lặp lịch học dựa trên thời gian của `Class`
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

    private TransactionHistory saveTransactionHistory(User user, Long amount, Wallet wallet) {
        TransactionHistory transactionHistory = new TransactionHistory();
        transactionHistory.setAmount(amount);
        transactionHistory.setTransactionDate(LocalDateTime.now());
        transactionHistory.setUser(user);
        transactionHistory.setTransactionBalance(wallet.getBalance());

        transactionHistoryRepository.save(transactionHistory);
        Context context = new Context();
        context.setVariable("transactionHistory", transactionHistory);
        emailService.sendEmail(user.getEmail(), "Transaction", "transaction-email", context);

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

        if (orderDetailRepository.countByClasses_ClassIdAndOrder_StatusNot(classId, OrderStatus.CANCELLED) >= maxStudents) {
            throw new Exception("Class is fully booked.");
        }
    }

    private double getMinimumPercentage() {
        System minimumPercentageParam = systemRepository.findByName("minimum_required_percentage");
        return minimumPercentageParam != null
                ? Double.parseDouble(minimumPercentageParam.getValue())
                : 0.8;
    }
}
