package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.Application;
import com.example.FPTLSPlatform.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ApplicationRepository extends JpaRepository<Application,Long> {
    Page<Application> getAll(Pageable pageable);
    @Query("SELECT a FROM Application a WHERE a.assignedStaff.userName = :username")
    Page<Application> findByAssignedStaffUserName(String staffUsername, Pageable pageable);

    Page<Application> findByStatusAndAssignedStaffIsNull(String status, Pageable pageable);

}
