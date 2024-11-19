package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.model.Teacher;
import com.example.FPTLSPlatform.model.User;
import com.example.FPTLSPlatform.request.RegisterRequest;

import java.util.List;
import java.util.Map;

public interface IUserService {
    void updateWalletBalance(String username, Double amount);
    Double getSystemWalletBalance();
    Map<String, Long> getUserCountByRole();
    List<User> getUsersByRoleStudent();
    User deactivateUser(String username);
    User createStaffUser(RegisterRequest request);
    Teacher getTeacher(String teacherName);
    Teacher deactivateTeacher(String teacherName);
    Map<String, List<Teacher>> getTeachersByStatus();

}
