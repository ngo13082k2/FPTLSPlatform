package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.OtherApplicationDTO;
import com.example.FPTLSPlatform.dto.WithdrawalRequestDTO;
import com.example.FPTLSPlatform.model.*;
import com.example.FPTLSPlatform.repository.*;
import com.example.FPTLSPlatform.service.IApplicationUserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
    public void processWithdrawalRequest(WithdrawalRequestDTO withdrawalRequestDto) {
        ApplicationType applicationType = applicationTypeRepository.findById(withdrawalRequestDto.getApplicationTypeId())
                .orElseThrow(() -> new RuntimeException("Application type not found"));

        User user = getLoggedInUser();

        double currentBalance = user.getWallet().getBalance();
        double withdrawalAmount = withdrawalRequestDto.getAmount();

        if (withdrawalAmount > currentBalance) {
            throw new RuntimeException("Số dư không đủ để rút tiền. Số dư hiện tại: " + currentBalance);
        }

        user.getWallet().setBalance(currentBalance - withdrawalAmount);
        walletRepository.save(user.getWallet());

        TransactionHistory transactionHistory = TransactionHistory.builder()
                .amount(withdrawalAmount)
                .transactionDate(LocalDateTime.now())
                .transactionBalance(user.getWallet().getBalance())
                .user(user)
                .build();
        transactionHistoryRepository.save(transactionHistory);

        ApplicationUser applicationUser = mapWithdrawalDtoToEntity(withdrawalRequestDto, applicationType, user);

        applicationUserRepository.save(applicationUser);
    }

    public String completeWithdrawalRequest(Long applicationUserId) {
        ApplicationUser applicationUser = applicationUserRepository.findById(applicationUserId)
                .orElseThrow(() -> new RuntimeException("Withdrawal request not found"));

        // Lấy số tiền từ description
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

        SystemTransactionHistory transactionHistory = SystemTransactionHistory.builder()
                .transactionAmount(amount)
                .transactionDate(LocalDateTime.now())
                .balanceAfterTransaction(systemWallet.getTotalAmount())
                .username(applicationUser.getUser().getUserName())
                .build();
        systemTransactionHistoryRepository.save(transactionHistory);

        // Đổi trạng thái của ApplicationUser thành completed
        applicationUser.setStatus("completed");
        applicationUserRepository.save(applicationUser);

        return "Đã trả tiền thành công và cập nhật trạng thái yêu cầu thành completed.";
    }




    public void processOtherRequest(OtherApplicationDTO otherRequestDto) {
        ApplicationType applicationType = applicationTypeRepository.findById(otherRequestDto.getApplicationTypeId())
                .orElseThrow(() -> new RuntimeException("Application type not found"));

        User user = getLoggedInUser();

        ApplicationUser applicationUser = mapOtherDtoToEntity(otherRequestDto, applicationType, user);

        applicationUserRepository.save(applicationUser);
    }
    public List<ApplicationUser> getApplicationsByType(Long applicationTypeId) {
        return applicationUserRepository.findByApplicationType_Id(applicationTypeId);
    }

    private User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
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
    private ApplicationUser mapWithdrawalDtoToEntity(WithdrawalRequestDTO dto, ApplicationType applicationType, User user) {
        return ApplicationUser.builder()
                .name(dto.getAccountHolderName())
                .title("Withdrawal Request")
                .description("Account number: " + dto.getAccountNumber() + ", Bank: " + dto.getBank() + ", Amount: " + dto.getAmount())
                .status("pending")
                .applicationType(applicationType)
                .user(user)
                .build();
    }


    private ApplicationUser mapOtherDtoToEntity(OtherApplicationDTO dto, ApplicationType applicationType, User user) {
        return ApplicationUser.builder()
                .name(dto.getStudentName())
                .title("Other Request")
                .description("Student Roll No: " + dto.getStudentRollNo() + ", Reason: " + dto.getReason())
                .status("pending")
                .applicationType(applicationType)
                .user(user)
                .build();
    }
}
