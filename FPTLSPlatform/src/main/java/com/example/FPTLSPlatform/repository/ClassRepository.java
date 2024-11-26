package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.dto.StudentDTO;
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

    boolean existsByTeacher_TeacherNameAndSlot_SlotIdAndDayOfWeekAndStartDate(
            String teacherName, Long slotId, String dayOfWeek, LocalDate startDate);

    Optional<Class> findById(Long classId);

    Page<Class> findByStatusAndStartDate(ClassStatus status, LocalDate date, Pageable pageable);

    List<Class> findByTeacherTeacherName(String teacherName);

    List<Class> findByTeacher(Teacher teacher);

    List<StudentDTO> findStudentsByClassId(Long classId);

    List<Class> findByStartDateAndStatus(LocalDate localDate, ClassStatus status);

    List<Class> findByCoursesCategoriesCategoryIdIn(Set<Long> categoryIds);

    List<Class> findByStatus(ClassStatus status);

    List<Class> findByStatusAndStartDateBetween(ClassStatus classStatus, LocalDate localDate, LocalDate localDate1);

    List<Class> findByTeacherTeacherNameAndStatus(String teacherName, ClassStatus status);
    
    boolean existsByCode(String string);

    boolean existsByTeacher_TeacherNameAndSlot_SlotIdAndDayOfWeekAndStartDateAndStatusNot(String teacherName, Long slotId, String dayOfWeek, LocalDate startDate, ClassStatus classStatus);
}
