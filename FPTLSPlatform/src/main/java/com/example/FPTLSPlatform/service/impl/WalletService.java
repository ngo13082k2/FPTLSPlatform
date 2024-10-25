package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.TransactionHistoryDTO;
import com.example.FPTLSPlatform.model.TransactionHistory;
import com.example.FPTLSPlatform.model.User;
import com.example.FPTLSPlatform.model.Wallet;
import com.example.FPTLSPlatform.repository.TransactionHistoryRepository;
import com.example.FPTLSPlatform.repository.UserRepository;
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

    public WalletService(UserRepository userRepository, TransactionHistoryRepository transactionHistoryRepository) {
        this.userRepository = userRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
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
                history.getUser().getUserName()
        );
    }

    public void refundToWallet(Long amount) throws Exception {
        Wallet wallet = getWalletByUserName();
        double currentBalance = wallet.getBalance();
        wallet.setBalance(currentBalance + amount);
        userRepository.save(wallet.getUser());

        saveTransactionHistory(wallet.getUser(), amount, "Hoàn tiền");
    }


    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }
    private void saveTransactionHistory(User user, Long amount, String description) {
        TransactionHistory transactionHistory = new TransactionHistory();
        transactionHistory.setAmount(amount);
        transactionHistory.setTransactionDate(LocalDateTime.now());
        transactionHistory.setUser(user);

        transactionHistoryRepository.save(transactionHistory);
    }
}
