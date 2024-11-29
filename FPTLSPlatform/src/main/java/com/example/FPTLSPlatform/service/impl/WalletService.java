package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.TransactionHistoryDTO;
import com.example.FPTLSPlatform.dto.WalletStatisticDTO;
import com.example.FPTLSPlatform.model.Teacher;
import com.example.FPTLSPlatform.model.TransactionHistory;
import com.example.FPTLSPlatform.model.User;
import com.example.FPTLSPlatform.model.Wallet;
import com.example.FPTLSPlatform.repository.*;
import com.example.FPTLSPlatform.service.IWalletService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WalletService implements IWalletService {
    private final UserRepository userRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final WalletRepository walletRepository;
    private final TeacherRepository teacherRepository;
    private final OrderRepository orderRepository;

    public WalletService(UserRepository userRepository, TransactionHistoryRepository transactionHistoryRepository, WalletRepository walletRepository, TeacherRepository teacherRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.walletRepository = walletRepository;
        this.teacherRepository = teacherRepository;
        this.orderRepository = orderRepository;
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

        // Kiểm tra xem username có phải là User hay không
        User user = userRepository.findByUserName(username).orElse(null);

        if (user != null) {
            List<TransactionHistory> histories = transactionHistoryRepository.findByUser(user);
            return histories.stream().map(this::mapToDTOUser).collect(Collectors.toList());
        } else {
            // Nếu không là User, kiểm tra xem username có phải là Teacher không
            Teacher teacher = teacherRepository.findByTeacherName(username)
                    .orElseThrow(() -> new Exception("Không tìm thấy người dùng hoặc giáo viên"));

            List<TransactionHistory> histories = transactionHistoryRepository.findByTeacher(teacher);
            return histories.stream().map(this::mapToDTOTeacher).collect(Collectors.toList());
        }
    }

    private TransactionHistoryDTO mapToDTOUser(TransactionHistory history) {


        return new TransactionHistoryDTO(
                history.getId(),
                history.getAmount(),
                history.getTransactionDate(),
                history.getTransactionBalance(),
                history.getUser().getUserName(),
                null,
                history.getNote()

        );
    }

    private TransactionHistoryDTO mapToDTOTeacher(TransactionHistory history) {


        return new TransactionHistoryDTO(
                history.getId(),
                history.getAmount(),
                history.getTransactionDate(),
                history.getTransactionBalance(),
                null,
                history.getTeacher().getTeacherName(),
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
        // Lấy dữ liệu từ ba truy vấn
        List<Object[]> lastBalances = walletRepository.getLastBalanceByMonth(year);
        List<Object[]> transactionSummaries = walletRepository.getTransactionSummaryByMonth(year);
        List<Object[]> orders = orderRepository.getTotalOrderByMonth(year);

        // Chuyển dữ liệu thành Map để dễ xử lý
        Map<Integer, Double> balanceMap = new HashMap<>();
        for (Object[] row : lastBalances) {
            Integer month = (Integer) row[0];
            Double lastBalance = (Double) row[1];
            balanceMap.put(month, lastBalance);
        }

        Map<Integer, Long> orderMap = new HashMap<>();
        for (Object[] row : orders) {
            Integer month = (Integer) row[0];
            Long totalOrders = (Long) row[1];
            orderMap.put(month, totalOrders);
        }

        // Tạo danh sách WalletStatisticDTO
        List<WalletStatisticDTO> statistics = new ArrayList<>();
        for (Object[] row : transactionSummaries) {
            Integer month = (Integer) row[0];
            Double totalIncome = (Double) row[1];
            Double totalExpense = (Double) row[2];

            // Lấy số dư cuối cùng từ balanceMap
            Double lastBalance = balanceMap.getOrDefault(month, 0.0);

            // Lấy tổng số đơn hàng từ orderMap
            Long totalOrders = orderMap.getOrDefault(month, 0L);

            // Tạo DTO và thêm vào danh sách
            WalletStatisticDTO dto = new WalletStatisticDTO(month, lastBalance, totalIncome, totalExpense, totalOrders);
            statistics.add(dto);
        }

        return statistics;
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
