package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    @Query("SELECT a FROM Application a WHERE a.assignedStaff.userName = :staffUsername ORDER BY a.applicationId DESC")
    Page<Application> findByAssignedStaffUserName(String staffUsername, Pageable pageable);

    Page<Application> findByStatusAndAssignedStaffIsNull(String status, Pageable pageable);

    List<Application> findByStatus(String status);

    Optional<Application> findByTeacher_TeacherNameAndStatusNot(String teacherName, String approved);
}
