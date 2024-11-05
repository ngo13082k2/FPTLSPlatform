package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.ClassDTO;
import com.example.FPTLSPlatform.dto.TotalOrderDTO;
import com.example.FPTLSPlatform.model.SystemTransactionHistory;
import com.example.FPTLSPlatform.model.SystemWallet;
import com.example.FPTLSPlatform.model.enums.ClassStatus;
import com.example.FPTLSPlatform.service.IClassService;
import com.example.FPTLSPlatform.service.IOrderService;
import com.example.FPTLSPlatform.service.ISystemWalletService;
import com.example.FPTLSPlatform.service.IUserService;
import com.example.FPTLSPlatform.service.impl.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private IUserService userService;
    @Autowired
    private ISystemWalletService systemWalletService;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private IClassService classService;

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
        List<ClassDTO> classes = classService.getClassesByStatusAndMonthDetailed(ClassStatus.ACTIVE, year,month);
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

}
