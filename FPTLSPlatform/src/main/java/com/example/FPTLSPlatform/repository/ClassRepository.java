package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.Class;
import com.example.FPTLSPlatform.model.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClassRepository extends JpaRepository<Class, Long> {
    List<Class> findByCoursesCourseCode(String courseCode);

    Optional<Class> findById(Long classId);

    @Query("SELECT c FROM Class c WHERE c.status = :status AND c.startDate <= :twoDaysFromNow")
    Page<Class> findByStatusAndStartDateBefore(@Param("status") String status, @Param("twoDaysFromNow") LocalDateTime twoDaysFromNow, Pageable pageable);

    List<Class> findByTeacherTeacherName(String teacherName);

    List<Class> findByTeacher(Teacher teacher);

    Page<Class> findByStartDate(LocalDate date, Pageable pageable);

    List<Class> findByStartDateBeforeAndStatus(LocalDate localDate, String ongoing);
}
