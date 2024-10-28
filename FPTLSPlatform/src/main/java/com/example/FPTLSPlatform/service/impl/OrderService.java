package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.*;
import com.example.FPTLSPlatform.exception.InsufficientBalanceException;
import com.example.FPTLSPlatform.exception.OrderAlreadyExistsException;
import com.example.FPTLSPlatform.exception.ResourceNotFoundException;
import com.example.FPTLSPlatform.model.Class;
import com.example.FPTLSPlatform.model.*;
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

    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(order -> new OrderDTO(order.getOrderId(), order.getUser().getUserName(), order.getCreateAt(), order.getTotalPrice(), order.getStatus()));
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
        order.setStatus(OrderStatus.PENDING);
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
                .classDTO(mapEntityToDTO(orderDetail.getClasses()))
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
                walletService.refundToWallet(order.getTotalPrice());
                saveTransactionHistory(order.getUser(), orderDetail.getPrice());
                order.setStatus(OrderStatus.CANCELLED);
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
        Page<Class> classesPage = classRepository.findByStatusAndStartDateBefore(ClassStatus.PENDING, twoDaysFromNow, pageable);

        for (Class scheduledClass : classesPage) {
            try {
                activateClassIfEligible(scheduledClass);
            } catch (MessagingException e) {
                log.error("Error sending activation email for class {}: {}", scheduledClass.getClassId(), e.getMessage());
            }
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void updateClassesToOngoing() {
        LocalDateTime now = LocalDateTime.now();

        List<Class> classesToStart = classRepository.findByStartDateBeforeAndStatus(now.toLocalDate(), ClassStatus.ACTIVE);

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

                scheduledClass.setStatus(ClassStatus.ONGOING);
                classRepository.save(scheduledClass);
                log.info("Class with ID {} has started and is now ONGOING.", scheduledClass.getClassId());

            }
        }
    }


    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void checkAndCompleteOrders() {
        LocalDateTime now = LocalDateTime.now();

        List<Class> classesToComplete = classRepository.findByStartDateBeforeAndStatus(now.toLocalDate(), ClassStatus.ONGOING);

        for (Class scheduledClass : classesToComplete) {
            LocalDateTime endTime = scheduledClass.getStartDate().atTime(scheduledClass.getSlot().getEndTime());

            if (now.isAfter(endTime)) {
                Page<OrderDetail> orderDetails = orderDetailRepository.findByClasses_ClassId(scheduledClass.getClassId(), Pageable.unpaged());
                for (OrderDetail orderDetail : orderDetails) {
                    Order order = orderDetail.getOrder();
                    if (order.getStatus().equals(OrderStatus.ONGOING)) {
                        order.setStatus(OrderStatus.COMPLETED);
                        orderRepository.save(order);
                    }
                }

                scheduledClass.setStatus(ClassStatus.COMPLETED);
                classRepository.save(scheduledClass);
                log.info("Class with ID {} has started and is now COMPLETED.", scheduledClass.getClassId());

            }
        }
    }

    private void activateClassIfEligible(Class scheduledClass) throws MessagingException {
        int registeredStudents = orderDetailRepository.countByClasses_ClassId(scheduledClass.getClassId());
        int minimumRequiredStudents = (int) (scheduledClass.getMaxStudents() * 0.8);

        if (registeredStudents >= minimumRequiredStudents) {
            scheduledClass.setStatus(ClassStatus.ACTIVE);
            classRepository.save(scheduledClass);
            log.info("Class with ID {} has been activated.", scheduledClass.getClassId());

            sendActivationEmail(scheduledClass);
            notificationService.createNotification(NotificationDTO.builder()
                    .title("Class" + scheduledClass.getCode() + "has been activated")
                    .description("Class" + scheduledClass.getCode() + "has been start on" + scheduledClass.getStartDate())
                    .name("Notification")
                    .build());
        } else {
            scheduledClass.setStatus(ClassStatus.CANCELED);
            classRepository.save(scheduledClass);
            log.info("Class with ID {} has been cancelled due to insufficient students. Only {} registered, minimum required is {}.",
                    scheduledClass.getClassId(), registeredStudents, minimumRequiredStudents);

            refundStudents(scheduledClass);
            notificationService.createNotification(NotificationDTO.builder()
                    .title("Class" + scheduledClass.getCode() + "has been cancelled")
                    .description("Class" + scheduledClass.getCode() + "has been cancelled due to insufficient students")
                    .name("Notification")
                    .build());
        }
    }


    private void refundStudents(Class cancelledClass) {
        Page<OrderDetail> orderDetails = orderDetailRepository.findByClasses_ClassId(cancelledClass.getClassId(), Pageable.unpaged());

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

    private ClassDTO mapEntityToDTO(Class clazz) {
        return ClassDTO.builder()
                .classId(clazz.getClassId())
                .name(clazz.getName())
                .code(clazz.getCode())
                .description(clazz.getDescription())
                .status(clazz.getStatus())
                .location(clazz.getLocation())
                .maxStudents(clazz.getMaxStudents())
                .createDate(clazz.getCreateDate())
                .slotId(clazz.getSlot().getSlotId())
                .price(clazz.getPrice())
                .dayOfWeek(clazz.getDayOfWeek())
                .teacherName(clazz.getTeacher().getTeacherName())
                .fullName(clazz.getTeacher().getFullName())
                .startDate(clazz.getStartDate())
//                .endDate(clazz.getEndDate())
                .courseCode(clazz.getCourses().getCourseCode())
                .build();
    }
}
