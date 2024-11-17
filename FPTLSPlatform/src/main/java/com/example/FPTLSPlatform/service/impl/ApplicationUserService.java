package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.NotificationDTO;
import com.example.FPTLSPlatform.dto.OtherApplicationDTO;
import com.example.FPTLSPlatform.dto.WithdrawalRequestDTO;
import com.example.FPTLSPlatform.model.*;
import com.example.FPTLSPlatform.repository.*;
import com.example.FPTLSPlatform.service.IApplicationUserService;
import com.example.FPTLSPlatform.service.IEmailService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ApplicationUserService implements IApplicationUserService {

    private final ApplicationUserRepository applicationUserRepository;

    private final ApplicationTypeRepository applicationTypeRepository;

    private final UserRepository userRepository;

    private final WalletRepository walletRepository;

    private final SystemTransactionHistoryRepository systemTransactionHistoryRepository;

    private final SystemWalletRepository systemWalletRepository;

    private final TransactionHistoryRepository transactionHistoryRepository;

    private final TeacherRepository teacherRepository;

    private final NotificationService notificationService;

    private final IEmailService emailService;

    @Autowired
    public ApplicationUserService(ApplicationUserRepository applicationUserRepository, ApplicationTypeRepository applicationTypeRepository, UserRepository userRepository, WalletRepository walletRepository, SystemTransactionHistoryRepository systemTransactionHistoryRepository, SystemWalletRepository systemWalletRepository, TransactionHistoryRepository transactionHistoryRepository, TeacherRepository teacherRepository, NotificationService notificationService, IEmailService emailService) {
        this.applicationUserRepository = applicationUserRepository;
        this.applicationTypeRepository = applicationTypeRepository;
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.systemTransactionHistoryRepository = systemTransactionHistoryRepository;
        this.systemWalletRepository = systemWalletRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.teacherRepository = teacherRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    public void processWithdrawalRequest(WithdrawalRequestDTO withdrawalRequestDto) {
        ApplicationType applicationType = applicationTypeRepository.findById(withdrawalRequestDto.getApplicationTypeId())
                .orElseThrow(() -> new RuntimeException("Application type not found"));

        // Lấy người dùng hiện tại từ cả bảng User và Teacher
        Object userOrTeacher = getLoggedInUserOrTeacher();
        double currentBalance;
        Wallet wallet;

        if (userOrTeacher instanceof User user) {
            currentBalance = user.getWallet().getBalance();
            wallet = user.getWallet();
        } else if (userOrTeacher instanceof Teacher teacher) {
            currentBalance = teacher.getWallet().getBalance();
            wallet = teacher.getWallet();
        } else {
            throw new RuntimeException("Invalid user type.");
        }

        double withdrawalAmount = withdrawalRequestDto.getAmount();

        if (withdrawalAmount > currentBalance) {
            throw new RuntimeException("Số dư không đủ để rút tiền. Số dư hiện tại: " + currentBalance);
        }

        wallet.setBalance(currentBalance - withdrawalAmount);
        walletRepository.save(wallet);

        TransactionHistory transactionHistory = TransactionHistory.builder()
                .amount(-withdrawalAmount)
                .transactionDate(LocalDateTime.now())
                .transactionBalance(wallet.getBalance())
                .user(userOrTeacher instanceof User ? (User) userOrTeacher : null)
                .teacher(userOrTeacher instanceof Teacher ? (Teacher) userOrTeacher : null)
                .note("WithdrawalRequest")
                .build();
        transactionHistoryRepository.save(transactionHistory);
        ApplicationUser applicationUser = mapWithdrawalDtoToEntity(withdrawalRequestDto, applicationType, userOrTeacher);
        if (applicationUser.getUser() != null) {
            notificationService.createNotification(NotificationDTO.builder()
                    .title("Create withdraw application successful")
                    .description("Create withdraw " + String.format("%.2f", Math.abs(transactionHistory.getAmount())) + " successful. Remaining account " + String.format("%.2f", Math.abs(transactionHistory.getTransactionBalance())))
                    .name("Application Notification")
                    .username(applicationUser.getUser().getUserName())
                    .type("Withdraw application")
                    .build());
        } else {
            notificationService.createNotification(NotificationDTO.builder()
                    .title("Create withdraw application successful")
                    .description("Create withdraw " + String.format("%.2f", Math.abs(transactionHistory.getAmount())) + " successful. Remaining account " + String.format("%.2f", Math.abs(transactionHistory.getTransactionBalance())))
                    .name("Application Notification")
                    .username(applicationUser.getTeacher().getTeacherName())
                    .type("Withdraw application")
                    .build());
        }
        applicationUserRepository.save(applicationUser);
    }

    public void cancelWithdrawalRequest(Long withdrawalRequestId) {
        ApplicationUser applicationUser = applicationUserRepository.findById(withdrawalRequestId)
                .orElseThrow(() -> new RuntimeException("Withdrawal request not found"));

        if (!"pending".equals(applicationUser.getStatus())) {
            throw new RuntimeException("Only pending withdrawal requests can be canceled.");
        }
        Double refundAmount = applicationUser.getAmountFromDescription();
        if (refundAmount == null || refundAmount <= 0) {
            throw new RuntimeException("Invalid or missing amount in the withdrawal request.");
        }
        Object userOrTeacher = applicationUser.getUser() != null ? applicationUser.getUser() : applicationUser.getTeacher();
        Wallet wallet;
        double currentBalance;

        if (userOrTeacher instanceof User user) {
            wallet = user.getWallet();
            currentBalance = wallet.getBalance();
        } else if (userOrTeacher instanceof Teacher teacher) {
            wallet = teacher.getWallet();
            currentBalance = wallet.getBalance();
        } else {
            throw new RuntimeException("Invalid user type.");
        }

        wallet.setBalance(currentBalance + refundAmount);
        walletRepository.save(wallet);

        TransactionHistory transactionHistory = TransactionHistory.builder()
                .amount(refundAmount)
                .transactionDate(LocalDateTime.now())
                .transactionBalance(wallet.getBalance())
                .user(userOrTeacher instanceof User ? (User) userOrTeacher : null)
                .teacher(userOrTeacher instanceof Teacher ? (Teacher) userOrTeacher : null)
                .note("WithdrawalRequest Canceled")
                .build();
        transactionHistoryRepository.save(transactionHistory);
        if (applicationUser.getUser() != null) {
            notificationService.createNotification(NotificationDTO.builder()
                    .title("Cancel withdraw application successful")
                    .description("Cancel withdraw " +
                            String.format("%.2f", Math.abs(transactionHistory.getAmount())) +
                            " successful. Remaining account " +
                            String.format("%.2f", Math.abs(transactionHistory.getTransactionBalance())))
                    .name("Application Notification")
                    .username(applicationUser.getUser().getUserName())
                    .type("Withdraw application")
                    .build());
        } else {
            notificationService.createNotification(NotificationDTO.builder()
                    .title("Cancel withdraw application successful")
                    .description("Cancel withdraw " +
                            String.format("%.2f", Math.abs(transactionHistory.getAmount())) +
                            " successful. Remaining account " +
                            transactionHistory.getTransactionBalance())
                    .name("Application Notification")
                    .username(applicationUser.getTeacher().getTeacherName())
                    .type("Withdraw application")
                    .build());
        }
        applicationUser.setStatus("Canceled");
        applicationUserRepository.save(applicationUser);
    }


    @Override
    public String approveApplication(Long applicationId) {
        ApplicationUser applicationUser = applicationUserRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        applicationUser.setStatus("completed");
        applicationUserRepository.save(applicationUser);
        notificationService.createNotification(NotificationDTO.builder()
                .title("Application approved")
                .description("Your application " + applicationUser.getName() + " has been approved.")
                .name("Notification")
                .username(applicationUser.getUser().getUserName())
                .type("Other application")
                .build());
        sendEmail(applicationUser);
        return "Your application has been approved.";
    }

    private void sendEmail(ApplicationUser applicationUser) {
        Context context = new Context();
        context.setVariable("application", applicationUser);
        if (applicationUser.getUser() != null) {
            emailService.sendEmail(applicationUser.getUser().getEmail(), "application", "application-email", context);
        }
        if (applicationUser.getTeacher() != null) {
            emailService.sendEmail(applicationUser.getTeacher().getEmail(), "application", "application-email", context);
        }
    }

    @Override
    public String rejectApplication(Long applicationId) {
        ApplicationUser applicationUser = applicationUserRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        applicationUser.setStatus("rejected");
        applicationUserRepository.save(applicationUser);
        notificationService.createNotification(NotificationDTO.builder()
                .title("Application rejected")
                .description("Your application " + applicationUser.getName() + " has been rejected")
                .name("Notification")
                .username(applicationUser.getUser().getUserName())
                .type("Other application")
                .build());
        sendEmail(applicationUser);
        return "Your application has been rejected.";
    }

    public String completeWithdrawalRequest(Long applicationUserId) {
        ApplicationUser applicationUser = applicationUserRepository.findById(applicationUserId)
                .orElseThrow(() -> new RuntimeException("Withdrawal request not found"));

        Double amount = applicationUser.getAmountFromDescription();
        if (amount == null) {
            throw new RuntimeException("Không thể xác định số tiền từ description.");
        }

        SystemWallet systemWallet = systemWalletRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("System wallet not found"));

        if (amount > systemWallet.getTotalAmount()) {
            throw new RuntimeException("Số dư hệ thống không đủ để trả tiền.");
        }

        systemWallet.setTotalAmount(systemWallet.getTotalAmount() - amount);
        systemWalletRepository.save(systemWallet);

        String username = null;
        if (applicationUser.getUser() != null) {
            username = applicationUser.getUser().getUserName();
        } else if (applicationUser.getTeacher() != null) {
            username = applicationUser.getTeacher().getTeacherName();
        } else {
            throw new RuntimeException("Invalid application user type, no associated User or Teacher.");
        }

        SystemTransactionHistory systemTransactionHistory = SystemTransactionHistory.builder()
                .transactionAmount(amount)
                .transactionDate(LocalDateTime.now())
                .balanceAfterTransaction(systemWallet.getTotalAmount())
                .username(username)
                .note("WithdrawalRequest Successful")
                .build();
        systemTransactionHistoryRepository.save(systemTransactionHistory);

        // Đổi trạng thái của ApplicationUser thành completed
        applicationUser.setStatus("completed");
        applicationUserRepository.save(applicationUser);
        notificationService.createNotification(NotificationDTO.builder()
                .title("Withdraw successful")
                .description("Withdraw successfully. Your bank account added " + String.format("%.2f", Math.abs(applicationUser.getAmountFromDescription())))
                .username(applicationUser.getUser().getUserName())
                .name("Notification")
                .type("Withdraw application")
                .build());
        Context context = new Context();
        context.setVariable("systemTransactionHistory", systemTransactionHistory);
        if (applicationUser.getUser() != null) {
            emailService.sendEmail(applicationUser.getUser().getEmail(), "Transaction", "withdraw-email", context);
        }
        if (applicationUser.getTeacher() != null) {
            emailService.sendEmail(applicationUser.getTeacher().getEmail(), "Transaction", "withdraw-email", context);
        }
        return "Đã trả tiền thành công và cập nhật trạng thái yêu cầu thành completed.";
    }


    public void processOtherRequest(OtherApplicationDTO otherRequestDto) {
        ApplicationType applicationType = applicationTypeRepository.findById(otherRequestDto.getApplicationTypeId())
                .orElseThrow(() -> new RuntimeException("Application type not found"));

        Object userOrTeacher = getLoggedInUserOrTeacher();

        ApplicationUser applicationUser = mapOtherDtoToEntity(otherRequestDto, applicationType, userOrTeacher);
        applicationUserRepository.save(applicationUser);
    }

    public List<ApplicationUser> getApplicationsByType(Long applicationTypeId) {
        return applicationUserRepository.findByApplicationType_Id(applicationTypeId);
    }

    public Object getLoggedInUserOrTeacher() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();

        Optional<User> studentUser = userRepository.findByUserName(username);
        if (studentUser.isPresent()) {
            return studentUser.get();
        }

        Optional<Teacher> teacherUser = teacherRepository.findByTeacherName(username);
        if (teacherUser.isPresent()) {
            return teacherUser.get();
        }

        throw new RuntimeException("User or Teacher not found");
    }

    @Transactional
    public String processWithdrawalPayment(Long applicationUserId) {
//        ApplicationUser applicationUser = applicationUserRepository.findById(applicationUserId)
//                .orElseThrow(() -> new RuntimeException("Application user not found"));
//
//        if (!"Withdrawal Request".equals(applicationUser.getTitle())) {
//            throw new RuntimeException("Application is not a withdrawal request");
//        }
//
//        User user = applicationUser.getUser();
//
//        double withdrawalAmount = Double.parseDouble(applicationUser.getDescription().split("Amount:")[1].trim());
//
//        if (user.getWallet().getBalance() < withdrawalAmount) {
//            throw new IllegalArgumentException("Không đủ số dư trong tài khoản để thực hiện thanh toán");
//        }
//
//        user.getWallet().setBalance(user.getWallet().getBalance() - withdrawalAmount);
//        applicationUser.setStatus("completed");
//
//        userRepository.save(user);
//        applicationUserRepository.save(applicationUser);
//
        return "Withdrawal request processed successfully";
    }

    public List<ApplicationUser> getApplicationUserByUserName() {
        Object loggedInUserOrTeacher = getLoggedInUserOrTeacher();
        List<ApplicationUser> applications;

        if (loggedInUserOrTeacher instanceof User user) {
            applications = applicationUserRepository.findByUser_UserName(user.getUserName());
        } else if (loggedInUserOrTeacher instanceof Teacher teacher) {
            applications = applicationUserRepository.findByTeacher_TeacherName(teacher.getTeacherName());
        } else {
            throw new RuntimeException("Invalid user type.");
        }

        if (applications.isEmpty()) {
            throw new RuntimeException("No applications found for the logged-in user.");
        }

        return applications;
    }


    private ApplicationUser mapWithdrawalDtoToEntity(WithdrawalRequestDTO dto, ApplicationType applicationType, Object userOrTeacher) {
        ApplicationUser.ApplicationUserBuilder builder = ApplicationUser.builder()
                .name(dto.getAccountHolderName())
                .title("Withdrawal Request")
                .description("Account number: " + dto.getAccountNumber() + ", Bank: " + dto.getBank() + ", Amount: " + dto.getAmount())
                .status("pending")
                .applicationType(applicationType);

        // Kiểm tra nếu userOrTeacher là User hoặc Teacher và gán cho ApplicationUser
        if (userOrTeacher instanceof User user) {
            builder.user(user);
        } else if (userOrTeacher instanceof Teacher teacher) {
            builder.teacher(teacher);
        } else {
            throw new IllegalArgumentException("Invalid user type");
        }

        return builder.build();
    }


    private ApplicationUser mapOtherDtoToEntity(OtherApplicationDTO dto, ApplicationType applicationType, Object userOrTeacher) {
        ApplicationUser.ApplicationUserBuilder builder = ApplicationUser.builder()
                .name(dto.getStudentName())
                .title("Other Request")
                .description("Student Roll No: " + dto.getStudentRollNo() + ", Reason: " + dto.getReason())
                .status("pending")
                .applicationType(applicationType);

        if (userOrTeacher instanceof User user) {
            builder.user(user);
        } else if (userOrTeacher instanceof Teacher teacher) {
            builder.teacher(teacher);
        } else {
            throw new IllegalArgumentException("Invalid user type");
        }

        return builder.build();
    }
}
