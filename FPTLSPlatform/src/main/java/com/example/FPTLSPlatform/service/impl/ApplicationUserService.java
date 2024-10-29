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

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private ApplicationTypeRepository applicationTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private SystemTransactionHistoryRepository systemTransactionHistoryRepository;

    @Autowired
    private SystemWalletRepository systemWalletRepository;

    @Autowired
    private TransactionHistoryRepository transactionHistoryRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private IEmailService emailService;

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
                .amount(withdrawalAmount)
                .transactionDate(LocalDateTime.now())
                .transactionBalance(wallet.getBalance())
                .user(userOrTeacher instanceof User ? (User) userOrTeacher : null)
                .teacher(userOrTeacher instanceof Teacher ? (Teacher) userOrTeacher : null)
                .build();
        transactionHistoryRepository.save(transactionHistory);

        ApplicationUser applicationUser = mapWithdrawalDtoToEntity(withdrawalRequestDto, applicationType, userOrTeacher);
        applicationUserRepository.save(applicationUser);
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
                .build();
        systemTransactionHistoryRepository.save(systemTransactionHistory);

        // Đổi trạng thái của ApplicationUser thành completed
        applicationUser.setStatus("completed");
        applicationUserRepository.save(applicationUser);
        notificationService.createNotification(NotificationDTO.builder()
                .title("Withdraw successful")
                .description("Rút tiền thành công")
                .name("Notification")
                .build());
        Context context = new Context();
        context.setVariable("systemTransactionHistory", systemTransactionHistory);
        emailService.sendEmail(applicationUser.getUser().getEmail(), "Transaction", "withdraw-email", context);
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
