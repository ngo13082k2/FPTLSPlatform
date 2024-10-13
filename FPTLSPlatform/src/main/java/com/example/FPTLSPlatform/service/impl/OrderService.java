package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.NotificationDTO;
import com.example.FPTLSPlatform.dto.OrderDTO;
import com.example.FPTLSPlatform.dto.ResponseDTO;
import com.example.FPTLSPlatform.exception.InsufficientBalanceException;
import com.example.FPTLSPlatform.exception.OrderAlreadyExistsException;
import com.example.FPTLSPlatform.exception.ResourceNotFoundException;
import com.example.FPTLSPlatform.model.Class;
import com.example.FPTLSPlatform.model.*;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public OrderService(OrderRepository orderRepository,
                        ClassRepository classRepository,
                        OrderDetailRepository orderDetailRepository,
                        UserRepository userRepository,
                        IWalletService walletService, IEmailService emailService,
                        INotificationService notificationService,
                        TransactionHistoryRepository transactionHistoryRepository) {
        this.orderRepository = orderRepository;
        this.classRepository = classRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.userRepository = userRepository;
        this.walletService = walletService;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.transactionHistoryRepository = transactionHistoryRepository;
    }

    @Override
    public OrderDTO createOrder(Long classId, String username) throws Exception {
        Class scheduledClass = getClassOrThrow(classId);
        User user = getUserOrThrow(username);

        checkOrderAlreadyExists(username, classId);

        Wallet wallet = walletService.getWalletByUserName();
        checkSufficientBalance(wallet, scheduledClass.getPrice());

        checkClassCapacity(classId, scheduledClass.getMaxStudents());

        Order order = new Order();
        order.setUser(user);
        order.setCreateAt(LocalDateTime.now());
        order.setStatus(String.valueOf(OrderStatus.PENDING));
        order.setTotalPrice(scheduledClass.getPrice());
        order = orderRepository.save(order);

        saveOrderDetail(order, scheduledClass);

        double newBalance = wallet.getBalance() - scheduledClass.getPrice();
        wallet.setBalance(newBalance);
        userRepository.save(wallet.getUser());

        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("class", scheduledClass);
        context.setVariable("teacherName", scheduledClass.getTeacher().getTeacherName());
        emailService.sendEmail(order.getUser().getUserName(), "Booking successful", "order-email", context);

        return OrderDTO.builder()
                .orderId(order.getOrderId())
                .username(order.getUser().getUserName())
                .createAt(order.getCreateAt())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .build();
    }

    @Override
    public List<OrderDTO> getOrdersByUser(String username) {
        List<Order> orders = orderRepository.findByUserName(username);
        return orders.stream()
                .map(order -> OrderDTO.builder()
                        .orderId(order.getOrderId())
                        .username(order.getUser().getUserName())
                        .createAt(order.getCreateAt())
                        .totalPrice(order.getTotalPrice())
                        .status(order.getStatus())
                        .build())
                .collect(Collectors.toList());
    }


    @Transactional
    public ResponseDTO<String> cancelOrder(Long orderId) {
        try {
            Order order = orderRepository.findById(String.valueOf(orderId))
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

            // Check if the order is already cancelled
            if (Objects.equals(order.getStatus(), String.valueOf(OrderStatus.CANCELLED))) {
                return new ResponseDTO<>("ERROR", "Cannot cancel the order because the order has already been cancelled.", null);
            }

            OrderDetail orderDetail = orderDetailRepository.findByOrder_OrderId(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order detail not found for order id: " + orderId));

            Class scheduledClass = orderDetail.getClasses();

            if (scheduledClass.getEndDate() == null) {
                return new ResponseDTO<>("ERROR", "Class end date is missing.", null);
            }

            // Check if the class has already ended
            if (scheduledClass.getEndDate().isBefore(LocalDateTime.now())) {
                return new ResponseDTO<>("ERROR", "Cannot cancel the order because the class has already ended.", null);
            }

            // Check if the order status is PENDING
            if (Objects.equals(order.getStatus(), String.valueOf(OrderStatus.PENDING))) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime cancelDeadline = scheduledClass.getEndDate().minusDays(2);

                // Check if cancellation is within 2 days of class ending
                if (now.isAfter(cancelDeadline)) {
                    return new ResponseDTO<>("ERROR", "Cannot cancel the order within 2 days before the class ends.", null);
                }

                // Refund and update order status
                walletService.refundToWallet(order.getTotalPrice());
                saveTransactionHistory(order.getUser(), orderDetail.getPrice());
                order.setStatus(String.valueOf(OrderStatus.CANCELLED));
                orderRepository.save(order);
                Context context = new Context();
                context.setVariable("username", order.getUser().getUserName());
                context.setVariable("class", scheduledClass);
                emailService.sendEmail(order.getUser().getUserName(), "Cancelled booking successful", "cancel-email", context);
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
        emailService.sendEmail(scheduledClass.getTeacher().getTeacherName(), "Class active", "active-email", context);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void checkAndActivateClasses() {
        LocalDateTime twoDaysFromNow = LocalDateTime.now().plusDays(2);
        Pageable pageable = PageRequest.of(0, 50);
        Page<Class> classesPage = classRepository.findByStatusAndStartDateBefore(String.valueOf(OrderStatus.PENDING), twoDaysFromNow, pageable);

        for (Class scheduledClass : classesPage) {
            try {
                activateClassIfEligible(scheduledClass);
            } catch (MessagingException e) {
                log.error("Error sending activation email for class {}: {}", scheduledClass.getClassId(), e.getMessage());
            }
        }
    }

    private void activateClassIfEligible(Class scheduledClass) throws MessagingException {
        int registeredStudents = orderDetailRepository.countByClasses_ClassId(scheduledClass.getClassId());
        int minimumRequiredStudents = (int) (scheduledClass.getMaxStudents() * 0.8);

        if (registeredStudents >= minimumRequiredStudents) {
            // Kích hoạt lớp học nếu đủ số lượng sinh viên
            scheduledClass.setStatus(String.valueOf(OrderStatus.ACTIVE));
            classRepository.save(scheduledClass);
            log.info("Class with ID {} has been activated.", scheduledClass.getClassId());

            sendActivationEmail(scheduledClass);
            notificationService.createNotification(NotificationDTO.builder()
                    .title("Class" + scheduledClass.getCode() + "has been activated")
                    .description("Class" + scheduledClass.getCode() + "has been start on" + scheduledClass.getStartDate())
                    .name("Notification")
                    .build());
        } else {
            // Hủy lớp học nếu không đủ số lượng sinh viên
            scheduledClass.setStatus(String.valueOf(OrderStatus.CANCELLED));
            classRepository.save(scheduledClass);
            log.info("Class with ID {} has been cancelled due to insufficient students. Only {} registered, minimum required is {}.",
                    scheduledClass.getClassId(), registeredStudents, minimumRequiredStudents);

            // Hoàn tiền cho các học sinh đã đăng ký
            refundStudents(scheduledClass);
            notificationService.createNotification(NotificationDTO.builder()
                    .title("Class" + scheduledClass.getCode() + "has been cancelled")
                    .description("Class" + scheduledClass.getCode() + "has been cancelled due to insufficient students")
                    .name("Notification")
                    .build());
        }
    }

    public List<String> notifyUpcomingClassesOnLogin(String username) {
        List<Class> upcomingClasses = getUpcomingClassesForUser(username);
        List<String> notifications = new ArrayList<>();

        for (Class scheduledClass : upcomingClasses) {
            String message = calculateTimeUntilClassStarts(scheduledClass);
            notifications.add(message);
        }

        return notifications;
    }


    private String calculateTimeUntilClassStarts(Class scheduledClass) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = scheduledClass.getStartDate();

        Duration duration = Duration.between(now, startDate);

        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        return String.format("Class %s starts in %d days, %d hours, and %d minutes.",
                scheduledClass.getCode(), days, hours, minutes);
    }

    public List<Class> getUpcomingClassesForUser(String username) {
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder_User_UserName(username);

        return orderDetails.stream()
                .map(OrderDetail::getClasses)
                .filter(scheduledClass -> scheduledClass.getStartDate().isAfter(LocalDateTime.now())) // Chỉ lấy các lớp học chưa bắt đầu
                .collect(Collectors.toList());
    }


    private void refundStudents(Class cancelledClass) {
        List<OrderDetail> orderDetails = orderDetailRepository.findByClasses_ClassId(cancelledClass.getClassId());

        for (OrderDetail orderDetail : orderDetails) {
            Order order = orderDetail.getOrder();
            User student = order.getUser();

            Wallet wallet = student.getWallet();
            wallet.setBalance(student.getWallet().getBalance() + (orderDetail.getPrice()));
            userRepository.save(student);
            saveTransactionHistory(wallet.getUser(), orderDetail.getPrice());

            log.info("Refunded {} to student {} for class {} cancellation.", orderDetail.getPrice(), student.getUserName(), cancelledClass.getClassId());
        }
    }

    private void saveTransactionHistory(User user, Long amount) {
        TransactionHistory transactionHistory = new TransactionHistory();
        transactionHistory.setAmount(amount);
        transactionHistory.setTransactionDate(LocalDateTime.now());
        transactionHistory.setUser(user);

        transactionHistoryRepository.save(transactionHistory);
    }

    private Class getClassOrThrow(Long classId) {
        return classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));
    }

    private User getUserOrThrow(String username) {
        return userRepository.findById(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    private void checkOrderAlreadyExists(String username, Long classId) {
        if (orderDetailRepository.existsByOrder_User_UserNameAndClasses_ClassId(username, classId)) {
            throw new OrderAlreadyExistsException("User has already registered for this class.");
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

    private void saveOrderDetail(Order order, Class scheduledClass) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrder(order);
        orderDetail.setClasses(scheduledClass);
        orderDetail.setPrice(scheduledClass.getPrice());
        orderDetailRepository.save(orderDetail);
    }

}
