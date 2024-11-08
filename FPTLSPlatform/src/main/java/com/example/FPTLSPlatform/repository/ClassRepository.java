package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.Class;
import com.example.FPTLSPlatform.model.Teacher;
import com.example.FPTLSPlatform.model.enums.ClassStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ClassRepository extends JpaRepository<Class, Long> {
    List<Class> findByCoursesCourseCode(String courseCode);

    boolean existsBySlot_SlotIdAndDayOfWeek(Long slotId, String dayOfWeek);

    boolean existsByTeacher_TeacherNameAndSlot_SlotIdAndDayOfWeek(String teacherName, Long slotId, String dayOfWeek);
    boolean existsByTeacher_TeacherNameAndSlot_SlotIdAndDayOfWeekAndStartDate(
            String teacherName, Long slotId, String dayOfWeek, LocalDate startDate);

    Optional<Class> findById(Long classId);

    @Query("SELECT c FROM Class c WHERE c.status = :status AND c.startDate <= :twoDaysFromNow")
    Page<Class> findByStatusAndStartDateBefore(@Param("status") ClassStatus status, @Param("twoDaysFromNow") LocalDate twoDaysFromNow, Pageable pageable);

    List<Class> findByTeacherTeacherName(String teacherName);

    List<Class> findByTeacher(Teacher teacher);

    Page<Class> findByStartDate(LocalDate date, Pageable pageable);

    List<Class> findByStartDateAndStatus(LocalDate localDate, ClassStatus status);

    List<Class> findByCoursesCategoriesCategoryIdIn(Set<Long> categoryIds);

    List<Class> findByStatus(ClassStatus status);

    List<Class> findByStatusAndStartDateBetween(ClassStatus classStatus, LocalDate localDate, LocalDate localDate1);
}
