package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.ClassDTO;
import com.example.FPTLSPlatform.dto.TotalOrderDTO;
import com.example.FPTLSPlatform.dto.WalletStatisticDTO;
import com.example.FPTLSPlatform.model.SystemTransactionHistory;
import com.example.FPTLSPlatform.model.enums.ClassStatus;
import com.example.FPTLSPlatform.service.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {


    private final IUserService userService;

    private final ISystemWalletService systemWalletService;

    private final IOrderService orderService;

    private final IClassService classService;

    private final IWalletService walletService;

    public AdminController(IUserService userService,
                           ISystemWalletService systemWalletService,
                           IOrderService orderService,
                           IClassService classService,
                           IWalletService walletService) {
        this.userService = userService;
        this.systemWalletService = systemWalletService;
        this.orderService = orderService;
        this.classService = classService;
        this.walletService = walletService;
    }

    @GetMapping("/system-wallet/balance")
    public Double getSystemWalletBalance() {
        return userService.getSystemWalletBalance();
    }

    @GetMapping("/system-wallet/transactions")
    public List<SystemTransactionHistory> getSystemWalletTransactionHistory() {
        return systemWalletService.getSystemWalletTransactionHistory();
    }

    @GetMapping("/user-count")
    public Map<String, Long> getUserCountByRole() {
        return userService.getUserCountByRole();
    }

    @GetMapping("/total")
    public TotalOrderDTO getTotalOrdersAndAmount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return orderService.getTotalOrders(startDate, endDate);
    }

    @GetMapping("/totalClass")
    public ResponseEntity<Long> getTotalClasses() {
        long totalClasses = classService.getTotalClasses();
        return ResponseEntity.ok(totalClasses);
    }

    @GetMapping("/statistics/active")
    public ResponseEntity<Map<YearMonth, Long>> getActiveClassesByMonth(@RequestParam(required = false) Integer year) {
        Map<YearMonth, Long> statistics = classService.getClassesByStatusAndMonth(ClassStatus.ACTIVE, year);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/statistics/ongoing")
    public ResponseEntity<Map<YearMonth, Long>> getOngoingClassesByMonth(@RequestParam(required = false) Integer year) {
        Map<YearMonth, Long> statistics = classService.getClassesByStatusAndMonth(ClassStatus.ONGOING, year);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/statistics/completed")
    public ResponseEntity<Map<YearMonth, Long>> getCompletedClassesByMonth(@RequestParam(required = false) Integer year) {
        Map<YearMonth, Long> statistics = classService.getClassesByStatusAndMonth(ClassStatus.COMPLETED, year);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/details/active")
    public ResponseEntity<List<ClassDTO>> getActiveClassesByMonthDetailed(
            @RequestParam int year, @RequestParam(required = false) Integer month) {
        List<ClassDTO> classes = classService.getClassesByStatusAndMonthDetailed(ClassStatus.ACTIVE, year, month);
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/details/ongoing")
    public ResponseEntity<List<ClassDTO>> getOngoingClassesByMonthDetailed(
            @RequestParam int year, @RequestParam(required = false) Integer month) {
        List<ClassDTO> classes = classService.getClassesByStatusAndMonthDetailed(ClassStatus.ONGOING, year, month);
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/details/completed")
    public ResponseEntity<List<ClassDTO>> getCompletedClassesByMonthDetailed(
            @RequestParam int year, @RequestParam(required = false) Integer month) {
        List<ClassDTO> classes = classService.getClassesByStatusAndMonthDetailed(ClassStatus.COMPLETED, year, month);
        return ResponseEntity.ok(classes);
    }

    @GetMapping("statistics/deposit")
    public ResponseEntity<?> getDepositsByMonth(@RequestParam(required = false) Integer year) {
        try {
            List<WalletStatisticDTO> walletStatisticDTO = walletService.getWalletStatistic(year);
            return ResponseEntity.ok(walletStatisticDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}
