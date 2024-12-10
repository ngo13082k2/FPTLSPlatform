package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.ClassDTO;
import com.example.FPTLSPlatform.dto.ListTotalOrderDTO;
import com.example.FPTLSPlatform.dto.WalletStatisticDTO;
import com.example.FPTLSPlatform.model.ApprovalRecord;
import com.example.FPTLSPlatform.model.SystemTransactionHistory;
import com.example.FPTLSPlatform.model.Teacher;
import com.example.FPTLSPlatform.model.User;
import com.example.FPTLSPlatform.model.enums.ClassStatus;
import com.example.FPTLSPlatform.request.RegisterRequest;
import com.example.FPTLSPlatform.service.*;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final ICategoryService categoryService;

    private final ICourseService courseService;
    private final IApplicationUserService applicationUserService;

    @Autowired
    public AdminController(IUserService userService,
                           ISystemWalletService systemWalletService,
                           IOrderService orderService,
                           IClassService classService,
                           IWalletService walletService, ICategoryService categoryService, ICourseService courseService, IApplicationUserService applicationUserService) {
        this.userService = userService;
        this.systemWalletService = systemWalletService;
        this.orderService = orderService;
        this.classService = classService;
        this.walletService = walletService;
        this.categoryService = categoryService;
        this.courseService = courseService;
        this.applicationUserService = applicationUserService;
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
    public ListTotalOrderDTO getTotalOrdersAndAmount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return orderService.getTotalOrders(startDate, endDate);
    }

    @GetMapping("/totalClass")
    public ResponseEntity<Long> getTotalClasses() {
        long totalClasses = classService.getTotalClasses();
        return ResponseEntity.ok(totalClasses);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Map<YearMonth, Long>>> getClassesByStatusAndMonth(@RequestParam(required = false) Integer year) {
        Map<String, Map<YearMonth, Long>> statistics = classService.getClassesGroupedByStatusAndMonths(year);
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

    @GetMapping("/details/canceled")
    public ResponseEntity<List<ClassDTO>> getCanceledClassesByMonthDetailed(
            @RequestParam int year, @RequestParam(required = false) Integer month) {
        List<ClassDTO> classes = classService.getClassesByStatusAndMonthDetailed(ClassStatus.CANCELED, year, month);
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

    @GetMapping("/total-categories")
    public ResponseEntity<Long> getTotalCategories() {
        long totalCategories = categoryService.getTotalCategories();
        return ResponseEntity.ok(totalCategories);
    }

    @GetMapping("/total-courses")
    public ResponseEntity<Long> getTotalCourses() {
        long totalCourses = courseService.getTotalCourses();
        return ResponseEntity.ok(totalCourses);
    }

    @GetMapping("/user")
    public ResponseEntity<List<User>> getStudents() {
        List<User> students = userService.getUsersByRoleStudentAndStaff();
        return ResponseEntity.ok(students);
    }

    @PutMapping("/{username}/UserNamedeactivate")
    public ResponseEntity<User> deactivateUser(@PathVariable String username) {
        User updatedUser = userService.deactivateUser(username);
        return ResponseEntity.ok(updatedUser);
    }


    @PostMapping("/register-staff")
    public ResponseEntity<User> registerStaff(@RequestBody RegisterRequest request) {
        try {
            User newStaff = userService.createStaffUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(newStaff);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/{teacherName}")
    public ResponseEntity<Teacher> getTeacher(@PathVariable String teacherName) {
        Teacher teacher = userService.getTeacher(teacherName);
        return ResponseEntity.ok(teacher);
    }

    @GetMapping
    public ResponseEntity<Map<String, List<Teacher>>> getTeachersByStatus() {
        Map<String, List<Teacher>> teachersByStatus = userService.getTeachersByStatus();
        return ResponseEntity.ok(teachersByStatus);
    }


    @PutMapping("/{teacherName}/deactivate")
    public ResponseEntity<Teacher> deactivateTeacher(@PathVariable String teacherName) {
        Teacher updatedTeacher = userService.deactivateTeacher(teacherName);
        return ResponseEntity.ok(updatedTeacher);
    }

    @PutMapping("/{classId}/complete")
    public ResponseEntity<String> completeClassImmediately(@PathVariable Long classId) {
        try {
            orderService.completeClassImmediately(classId);
            return ResponseEntity.ok("Lesson completed successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while completing the class.");
        }
    }

    @PutMapping("/{classId}/start")
    public ResponseEntity<String> activeClassImmediately(@PathVariable Long classId) {
        try {
            orderService.startClass(classId);
            return ResponseEntity.ok("Lesson active successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while active the class.");
        }
    }

    @GetMapping("/getAprroveRecord/{applicationUserId}")
    public ResponseEntity<ApprovalRecord> getApprovalRecordByApplicationUserId(@PathVariable Long applicationUserId) {
        ApprovalRecord approvalRecord = applicationUserService.getApprovalRecordByApplicationUserId(applicationUserId);
        return ResponseEntity.ok(approvalRecord);
    }
}
