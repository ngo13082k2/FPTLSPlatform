package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.TransactionHistoryDTO;
import com.example.FPTLSPlatform.dto.WalletStatisticDTO;
import com.example.FPTLSPlatform.model.Teacher;
import com.example.FPTLSPlatform.model.TransactionHistory;
import com.example.FPTLSPlatform.model.User;
import com.example.FPTLSPlatform.model.Wallet;
import com.example.FPTLSPlatform.repository.TeacherRepository;
import com.example.FPTLSPlatform.repository.TransactionHistoryRepository;
import com.example.FPTLSPlatform.repository.UserRepository;
import com.example.FPTLSPlatform.repository.WalletRepository;
import com.example.FPTLSPlatform.service.IWalletService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WalletService implements IWalletService {
    private final UserRepository userRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final WalletRepository walletRepository;
    private final TeacherRepository teacherRepository;

    public WalletService(UserRepository userRepository, TransactionHistoryRepository transactionHistoryRepository, WalletRepository walletRepository, TeacherRepository teacherRepository) {
        this.userRepository = userRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.walletRepository = walletRepository;
        this.teacherRepository = teacherRepository;
    }

    public Wallet getWalletByUserName() throws Exception {
        String username = getCurrentUsername();

        Optional<User> optionalUser = userRepository.findByUserName(username);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (user.getWallet() != null) {
                return user.getWallet();
            } else {
                throw new Exception("Người dùng không có ví.");
            }
        } else {
            throw new Exception("Không tìm thấy người dùng: " + username);
        }
    }
    public Wallet getWalletByTeacherName() throws Exception {
        String username = getCurrentUsername();
        Optional<Teacher> optionalTeacher = teacherRepository.findById(username);

        if (optionalTeacher.isPresent()) {
            Teacher teacher = optionalTeacher.get();

            if (teacher.getWallet() != null) {
                return teacher.getWallet();
            } else {
                throw new Exception("Giáo viên không có ví.");
            }
        } else {
            throw new Exception("Không tìm thấy giáo viên: " + username);
        }
    }


    public List<TransactionHistoryDTO> getTransactionHistory() throws Exception {
        String username = getCurrentUsername();

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new Exception("Không tìm thấy người dùng"));

        List<TransactionHistory> histories = transactionHistoryRepository.findByUser(user);
        return histories.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private TransactionHistoryDTO mapToDTO(TransactionHistory history) {
        return new TransactionHistoryDTO(
                history.getId(),
                history.getAmount(),
                history.getTransactionDate(),
                history.getTransactionBalance(),
                history.getUser().getUserName(),
                history.getNote()

        );
    }

    public void refundToWallet(Long amount) throws Exception {
        Wallet wallet = getWalletByUserName();
        double balance = wallet.getBalance();
        wallet.setBalance(balance + amount);
        userRepository.save(wallet.getUser());

        saveTransactionHistory(wallet.getUser(), amount, wallet.getBalance());
    }

    @Override
    public List<WalletStatisticDTO> getWalletStatistic(Integer year) {
        return walletRepository.getWalletStatisticByMonth(year);
    }


    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    private void saveTransactionHistory(User user, Long amount, Double balance) {
        TransactionHistory transactionHistory = new TransactionHistory();
        transactionHistory.setAmount(amount);
        transactionHistory.setTransactionBalance(balance);
        transactionHistory.setTransactionDate(LocalDateTime.now());
        transactionHistory.setUser(user);
        transactionHistory.setNote("Refunded");

        transactionHistoryRepository.save(transactionHistory);
    }
}
