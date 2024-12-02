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
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;

import java.io.IOException;
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
    private final CloudinaryService cloudinaryService;
    private final ApprovalRecordRepository approvalRecordRepository;

    @Autowired
    public ApplicationUserService(ApplicationUserRepository applicationUserRepository, ApplicationTypeRepository applicationTypeRepository, UserRepository userRepository, WalletRepository walletRepository, SystemTransactionHistoryRepository systemTransactionHistoryRepository, SystemWalletRepository systemWalletRepository, TransactionHistoryRepository transactionHistoryRepository, TeacherRepository teacherRepository, NotificationService notificationService, IEmailService emailService, CloudinaryService cloudinaryService, ApprovalRecordRepository approvalRecordRepository) {
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
        this.cloudinaryService = cloudinaryService;
        this.approvalRecordRepository = approvalRecordRepository;
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
                .note("Withdrawal")
                .build();
        transactionHistoryRepository.save(transactionHistory);
        ApplicationUser applicationUser = mapWithdrawalDtoToEntity(withdrawalRequestDto, applicationType, userOrTeacher);
        applicationUser.setCreatedDate(LocalDateTime.now());
        if (applicationUser.getUser() != null) {
            notificationService.createNotification(NotificationDTO.builder()
                    .title("Create withdraw application successful")
                    .description("Create withdraw " + formatToVND(transactionHistory.getAmount()) + " successful. Remaining account " + formatToVND(transactionHistory.getTransactionBalance()))
                    .name("Application Notification")
                    .username(applicationUser.getUser().getUserName())
                    .type("Withdraw application")
                    .build());
        } else {
            notificationService.createNotification(NotificationDTO.builder()
                    .title("Create withdraw application successful")
                    .description("Create withdraw " + formatToVND(transactionHistory.getAmount()) + " successful. Remaining account " + formatToVND(transactionHistory.getTransactionBalance()))
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
                .note("Withdrawal - Canceled")
                .build();
        transactionHistoryRepository.save(transactionHistory);
        if (applicationUser.getUser() != null) {
            notificationService.createNotification(NotificationDTO.builder()
                    .title("Cancel withdraw application successful")
                    .description("Cancel withdraw " +
                            formatToVND(transactionHistory.getAmount()) +
                            " successful. Remaining account " +
                            formatToVND(transactionHistory.getTransactionBalance()))
                    .name("Application Notification")
                    .username(applicationUser.getUser().getUserName())
                    .type("Withdraw application")
                    .build());
        } else {
            notificationService.createNotification(NotificationDTO.builder()
                    .title("Cancel withdraw application successful")
                    .description("Cancel withdraw " +
                            formatToVND(transactionHistory.getAmount()) +
                            " successful. Remaining account " +
                            formatToVND(transactionHistory.getTransactionBalance()))
                    .name("Application Notification")
                    .username(applicationUser.getTeacher().getTeacherName())
                    .type("Withdraw application")
                    .build());
        }
        applicationUser.setStatus("Canceled");
        applicationUserRepository.save(applicationUser);
    }


    @Override
    public String approveApplication(Long applicationId, MultipartFile approvalImage) throws IOException {
        ApplicationUser applicationUser = applicationUserRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        String username;
        String email;
        if (applicationUser.getUser() != null) {
            username = applicationUser.getUser().getUserName();
            email = applicationUser.getUser().getEmail();
        } else if (applicationUser.getTeacher() != null) {
            username = applicationUser.getTeacher().getTeacherName();
            email = applicationUser.getTeacher().getEmail();
        } else {
            throw new RuntimeException("Invalid application user type: no associated User or Teacher.");
        }

        // Cập nhật trạng thái đơn xin
        applicationUser.setStatus("completed");
        applicationUserRepository.save(applicationUser);

        // Xử lý nếu có hình ảnh phê duyệt
        if (approvalImage != null && !approvalImage.isEmpty()) {
            String approvalImageUrl = cloudinaryService.uploadImage(approvalImage);

            // Lưu thông tin phê duyệt vào ApprovalRecord
            ApprovalRecord approvalRecord = ApprovalRecord.builder()
                    .applicationUser(applicationUser)
                    .approvedBy(getCurrentUsername())  // Lấy người phê duyệt
                    .approvalImage(approvalImageUrl)
                    .approvalDate(LocalDateTime.now())
                    .build();
            approvalRecordRepository.save(approvalRecord);
        }

        // Tạo thông báo cho người dùng
        notificationService.createNotification(NotificationDTO.builder()
                .title("Application approved")
                .description("Your application " + username + " has been approved.")
                .name("Notification")
                .username(username)
                .type("Other application")
                .build());

        // Gửi email cho người dùng
        sendEmail(applicationUser, email, username);

        return "Your application has been approved.";
    }

    private void sendEmail(ApplicationUser applicationUser, String email, String username) {
        // Lấy toàn bộ mô tả
        String description = applicationUser.getDescription();
        String reason = extractReason(description); // Hàm lấy "Reason" từ chuỗi

        // Tạo Context cho email
        Context context = new Context();
        context.setVariable("application", applicationUser);
        context.setVariable("content", reason); // Gửi phần "Reason" trong email
        context.setVariable("username", username);
        // Gửi email
        emailService.sendEmail(email, "Application Notification", "application-email", context);
    }

    // Hàm tách "Reason"
    private String extractReason(String description) {
        if (description == null || description.isEmpty()) {
            return "No reason provided";
        }

        // Tìm vị trí "Reason: " và lấy phần còn lại
        String prefix = "Reason: ";
        int index = description.indexOf(prefix);
        if (index != -1) {
            return description.substring(index + prefix.length()).trim(); // Lấy phần sau "Reason: "
        }
        return "No reason found"; // Nếu không tìm thấy "Reason: "
    }


    @Override
    public String rejectApplication(Long applicationId) {
        ApplicationUser applicationUser = applicationUserRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        String username;
        String email;
        if (applicationUser.getUser() != null) {
            username = applicationUser.getUser().getUserName();
            email = applicationUser.getUser().getEmail();
        } else if (applicationUser.getTeacher() != null) {
            username = applicationUser.getTeacher().getTeacherName();
            email = applicationUser.getTeacher().getEmail();
        } else {
            throw new RuntimeException("Invalid application user type: no associated User or Teacher.");
        }
        applicationUser.setStatus("rejected");
        applicationUserRepository.save(applicationUser);
        notificationService.createNotification(NotificationDTO.builder()
                .title("Application rejected")
                .description("Your application " + username + " has been rejected")
                .name("Notification")
                .username(username)
                .type("Other application")
                .build());
        sendEmail(applicationUser, email, username);
        return "Your application has been rejected.";
    }

    public String completeWithdrawalRequestWithApproval(Long applicationUserId, MultipartFile approvalImage) throws IOException {
        ApplicationUser applicationUser = applicationUserRepository.findById(applicationUserId)
                .orElseThrow(() -> new RuntimeException("Withdrawal request not found"));

        Double amount = applicationUser.getAmountFromDescription();
        if (amount == null || amount <= 0) {
            throw new RuntimeException("Không thể xác định số tiền từ description hoặc số tiền không hợp lệ.");
        }

        SystemWallet systemWallet = systemWalletRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("System wallet not found"));

        if (amount > systemWallet.getTotalAmount()) {
            throw new RuntimeException("Số dư hệ thống không đủ để trả tiền.");
        }

        systemWallet.setTotalAmount(systemWallet.getTotalAmount() - amount);
        systemWalletRepository.save(systemWallet);

        String username;
        String email;
        if (applicationUser.getUser() != null) {
            username = applicationUser.getUser().getUserName();
            email = applicationUser.getUser().getEmail();
        } else if (applicationUser.getTeacher() != null) {
            username = applicationUser.getTeacher().getTeacherName();
            email = applicationUser.getTeacher().getEmail();
        } else {
            throw new RuntimeException("Invalid application user type: no associated User or Teacher.");
        }

        String approvedBy = getCurrentUsername();

        String approvalImageUrl = null;
        if (approvalImage != null && !approvalImage.isEmpty()) {
            approvalImageUrl = cloudinaryService.uploadImage(approvalImage);

            ApprovalRecord approvalRecord = ApprovalRecord.builder()
                    .applicationUser(applicationUser)
                    .approvedBy(approvedBy)
                    .approvalImage(approvalImageUrl)
                    .approvalDate(LocalDateTime.now())
                    .build();
            approvalRecordRepository.save(approvalRecord);
        }

        SystemTransactionHistory systemTransactionHistory = SystemTransactionHistory.builder()
                .transactionAmount(-amount)
                .transactionDate(LocalDateTime.now())
                .balanceAfterTransaction(systemWallet.getTotalAmount())
                .username(username)
                .note("Withdraw")
                .build();
        systemTransactionHistoryRepository.save(systemTransactionHistory);

        applicationUser.setStatus("completed");
        applicationUserRepository.save(applicationUser);
        Object userOrTeacher = applicationUser.getUser() != null ? applicationUser.getUser() : applicationUser.getTeacher();
        Wallet wallet;

        if (userOrTeacher instanceof User user) {
            wallet = user.getWallet();
        } else if (userOrTeacher instanceof Teacher teacher) {
            wallet = teacher.getWallet();
        } else {
            throw new RuntimeException("Invalid user type.");
        }
        // Tạo thông báo
        notificationService.createNotification(NotificationDTO.builder()
                .title("Withdraw successful")
                .description("Withdraw successfully. Your bank account added " +
                        formatToVND(amount) +
                        ". Remaining wallet: " +
                        formatToVND(wallet.getBalance()))
                .username(username)
                .name("Notification")
                .type("Withdraw application")
                .build());

        Context context = new Context();
        context.setVariable("systemTransactionHistory", systemTransactionHistory);
        emailService.sendEmail(email, "Transaction Successful", "withdraw-email", context);

        return "Đã trả tiền thành công và cập nhật trạng thái yêu cầu thành completed.";
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }


    public void processOtherRequest(OtherApplicationDTO otherRequestDto) {
        ApplicationType applicationType = applicationTypeRepository.findById(otherRequestDto.getApplicationTypeId())
                .orElseThrow(() -> new RuntimeException("Application type not found"));

        Object userOrTeacher = getLoggedInUserOrTeacher();

        ApplicationUser applicationUser = mapOtherDtoToEntity(otherRequestDto, applicationType, userOrTeacher);
        applicationUser.setCreatedDate(LocalDateTime.now());
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

    public static String formatToVND(double amount) {
        return OrderService.formatToVND(amount);
    }

    public ApprovalRecord getApprovalRecordByApplicationUserId(Long applicationUserId) {
        return approvalRecordRepository.findByApplicationUser_ApplicationUserId(applicationUserId)
                .orElseThrow(() -> new RuntimeException("ApprovalRecord not found for ApplicationUserId: " + applicationUserId));
    }
}
