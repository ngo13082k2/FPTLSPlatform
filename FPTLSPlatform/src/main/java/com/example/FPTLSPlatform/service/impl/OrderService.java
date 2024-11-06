package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.*;
import com.example.FPTLSPlatform.exception.InsufficientBalanceException;
import com.example.FPTLSPlatform.exception.OrderAlreadyExistsException;
import com.example.FPTLSPlatform.exception.ResourceNotFoundException;
import com.example.FPTLSPlatform.model.Class;
import com.example.FPTLSPlatform.model.*;
import com.example.FPTLSPlatform.model.System;
import com.example.FPTLSPlatform.model.enums.ClassStatus;
import com.example.FPTLSPlatform.model.enums.OrderStatus;
import com.example.FPTLSPlatform.repository.*;
import com.example.FPTLSPlatform.service.IEmailService;
import com.example.FPTLSPlatform.service.INotificationService;
import com.example.FPTLSPlatform.service.IOrderService;
import com.example.FPTLSPlatform.service.IWalletService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);


    public OrderService(OrderRepository orderRepository,
                        ClassRepository classRepository,
                        OrderDetailRepository orderDetailRepository,
                        UserRepository userRepository,
                        IWalletService walletService, IEmailService emailService,
                        INotificationService notificationService,
                        TransactionHistoryRepository transactionHistoryRepository,
                        SystemWalletRepository systemWalletRepository,
                        ClassService classService, WalletRepository walletRepository, SystemRepository systemRepository) {
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

        checkOrderAlreadyExists(username, classId);

        SystemWallet systemWallet = systemWalletRepository.getReferenceById(1L);
        Wallet wallet = walletService.getWalletByUserName();
        checkSufficientBalance(wallet, scheduleClass.getPrice());
        checkClassCapacity(classId, scheduleClass.getMaxStudents());

        Order order = new Order();
        order.setUser(user);
        order.setCreateAt(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(scheduleClass.getPrice());
        order = orderRepository.save(order);
        saveOrderDetail(order, scheduleClass);

        wallet.setBalance(wallet.getBalance() - scheduleClass.getPrice());
        systemWallet.setTotalAmount(systemWallet.getTotalAmount() + scheduleClass.getPrice());
        systemWalletRepository.save(systemWallet);
        TransactionHistory transactionHistory = saveTransactionHistory(order.getUser(), -order.getTotalPrice(), wallet);
        transactionHistory.setNote("Order");
        userRepository.save(wallet.getUser());

        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("class", scheduleClass);
        context.setVariable("teacherName", scheduleClass.getTeacher().getTeacherName());
        emailService.sendEmail(order.getUser().getEmail(), "Booking successful", "order-email", context);

        return OrderDTO.builder()
                .orderId(order.getOrderId())
                .username(order.getUser().getUserName())
                .createAt(order.getCreateAt())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .build();
    }

    private void saveOrderDetail(Order order, Class scheduledClass) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrder(order);
        orderDetail.setClasses(scheduledClass);
        orderDetail.setPrice(scheduledClass.getPrice());
        orderDetailRepository.save(orderDetail);
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
                .orderId(orderDetail.getOrder().getOrderId())
                .classDTO(classService.mapEntityToDTO(orderDetail.getClasses()))
                .price(orderDetail.getPrice())
                .build());
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
                LocalDate now = LocalDate.now();
                LocalDate cancelDeadline = scheduledClass.getStartDate().minusDays(2);

                if (now.isAfter(cancelDeadline)) {
                    return new ResponseDTO<>("ERROR", "Cannot cancel the order within 2 days before the class starts.", null);
                }

                // Refund and update order status
                Wallet wallet = walletService.getWalletByUserName();
                walletService.refundToWallet(order.getTotalPrice());
                TransactionHistory transactionHistory = saveTransactionHistory(order.getUser(), orderDetail.getPrice(), wallet);
                transactionHistory.setNote("Refunded");
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
                Context context = new Context();
                context.setVariable("username", order.getUser().getUserName());
                context.setVariable("class", scheduledClass);
                emailService.sendEmail(order.getUser().getEmail(), "Cancelled booking successful", "cancel-email", context);
                return new ResponseDTO<>("SUCCESS", "Order cancelled successfully", null);
            }

            return new ResponseDTO<>("ERROR", "Cannot cancel the order with current status.", null);

        } catch (Exception ex) {
            return new ResponseDTO<>("ERROR", ex.getMessage(), null);
        }
    }

    private void sendActivationEmail(Class scheduledClass) {
        Context context = new Context();
        context.setVariable("teacherName", scheduledClass.getTeacher().getTeacherName());
        context.setVariable("class", scheduledClass);
        emailService.sendEmail(scheduledClass.getTeacher().getEmail(), "Class active", "active-email", context);
    }

    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void checkAndActivateClasses() {
        LocalDateTime twoDaysFromNow = LocalDateTime.now().plusDays(2);
        Pageable pageable = PageRequest.of(0, 50);
        LocalDate dateToCheck = twoDaysFromNow.toLocalDate();

        Page<Class> classesPage = classRepository.findByStatusAndStartDateBefore(ClassStatus.PENDING, dateToCheck, pageable);

        for (Class scheduledClass : classesPage) {
            try {
                activateClassIfEligible(scheduledClass);

                log.info("Class with ID {} has been activated successfully.", scheduledClass.getClassId());
            } catch (MessagingException e) {
                log.error("Error sending activation email for class {}: {}", scheduledClass.getClassId(), e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected error occurred while activating class {}: {}", scheduledClass.getClassId(), e.getMessage());
            }
        }

        log.info("Checked and processed {} classes for activation.", classesPage.getNumberOfElements());
    }


    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void updateClassesToOngoing() {
        LocalDateTime now = LocalDateTime.now();

        List<Class> classesToStart = classRepository.findByStartDateAndStatus(now.toLocalDate(), ClassStatus.ACTIVE);

        for (Class scheduledClass : classesToStart) {
            LocalDateTime startTime = scheduledClass.getStartDate().atTime(scheduledClass.getSlot().getStartTime());

            if (now.isAfter(startTime)) {
                Page<OrderDetail> orderDetails = orderDetailRepository.findByClasses_ClassId(scheduledClass.getClassId(), Pageable.unpaged());
                for (OrderDetail orderDetail : orderDetails) {
                    Order order = orderDetail.getOrder();
                    if (order.getStatus().equals(OrderStatus.ACTIVE)) {
                        order.setStatus(OrderStatus.ONGOING);
                        orderRepository.save(order);
                    }
                }

                if (scheduledClass.getStatus().equals(ClassStatus.ACTIVE)) {
                    scheduledClass.setStatus(ClassStatus.ONGOING);
                }
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
            LocalDateTime endTime = scheduledClass.getEndDate().atTime(scheduledClass.getSlot().getEndTime());

            if (now.isAfter(endTime)) {
                Page<OrderDetail> orderDetails = orderDetailRepository.findByClasses_ClassId(scheduledClass.getClassId(), Pageable.unpaged());
                for (OrderDetail orderDetail : orderDetails) {
                    Order order = orderDetail.getOrder();
                    if (order.getStatus().equals(OrderStatus.ONGOING)) {
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
                                .name("Notification")
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

    private void activateClassIfEligible(Class scheduledClass) throws MessagingException {
        int registeredStudents = orderDetailRepository.countByClasses_ClassId(scheduledClass.getClassId());
        double defaultMinimumPercentage = 0.8;
        System minimumPercentageParam = systemRepository.findByName("minimum_required_percentage");
        double minimumPercentage = minimumPercentageParam != null
                ? Double.parseDouble(minimumPercentageParam.getValue())
                : defaultMinimumPercentage;
        int minimumRequiredStudents = (int) (scheduledClass.getMaxStudents() * minimumPercentage);
        if (registeredStudents >= minimumRequiredStudents) {
            Page<OrderDetail> orderDetails = orderDetailRepository.findByClasses_ClassId(scheduledClass.getClassId(), Pageable.unpaged());
            for (OrderDetail orderDetail : orderDetails) {
                orderDetail.getOrder().setStatus(OrderStatus.ACTIVE);
            }

            scheduledClass.setStatus(ClassStatus.ACTIVE);
            classRepository.save(scheduledClass);
            orderDetailRepository.saveAll(orderDetails);

            sendActivationEmail(scheduledClass);
            notificationService.createNotification(NotificationDTO.builder()
                    .title("Class " + scheduledClass.getCode() + " has been activated")
                    .description("Class " + scheduledClass.getCode() + " has been start on" + scheduledClass.getStartDate())
                    .name("Notification")
                    .build());
        } else {
            scheduledClass.setStatus(ClassStatus.CANCELED);
            classRepository.save(scheduledClass);
            refundStudents(scheduledClass);
        }
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
                    .name("Refund Notification")
                    .build());

        }
    }


    public boolean hasDuplicateSchedule(String username, Long classId) {
        Page<OrderDetail> userOrders = orderDetailRepository.findByOrder_User_UserName(username, Pageable.unpaged());

        Class newClass = classRepository.getReferenceById(classId);

        for (OrderDetail orderDetail : userOrders) {
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

        notificationService.createNotification(NotificationDTO.builder()
                .title("Funds Added to Wallet")
                .description("An amount of " + amount + " has been added to your wallet.")
                .name("Wallet Notification")
                .build());

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

    private void checkOrderAlreadyExists(String username, Long classId) {
        if (orderDetailRepository.existsByOrder_User_UserNameAndClasses_ClassId(username, classId)) {
            throw new OrderAlreadyExistsException("User has already registered for this class.");
        }
        if (hasDuplicateSchedule(username, classId)) {
            throw new IllegalStateException("User has already registered for this schedule.");
        }
    }

    private void checkSufficientBalance(Wallet wallet, double requiredAmount) {
        if (wallet.getBalance() < requiredAmount) {
            throw new InsufficientBalanceException("Wallet balance is not enough to register for class.");
        }
    }

    private void checkClassCapacity(Long classId, int maxStudents) throws Exception {
        if (orderDetailRepository.countByClasses_ClassId(classId) >= maxStudents) {
            throw new Exception("Class is fully booked.");
        }
    }

}
