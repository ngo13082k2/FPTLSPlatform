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

//    boolean existsByTeacher_TeacherNameAndSlot_SlotIdAndDayOfWeekAndStartDate(
//            String teacherName, Long slotId, String dayOfWeek, LocalDate startDate);

    Optional<Class> findById(Long classId);

    @Query("SELECT c FROM Class c JOIN c.dateSlots ds WHERE c.status = :status AND ds.date = :date")
    Page<Class> findByStatusAndStartDate(ClassStatus status, LocalDate date, Pageable pageable);

    List<Class> findByTeacherTeacherName(String teacherName);

    List<Class> findByTeacher(Teacher teacher);

    @Query("SELECT new com.example.FPTLSPlatform.dto.StudentDTO(s.userName, s.phoneNumber, s.email, s.fullName, s.address) " +
            "FROM OrderDetail od " +
            "JOIN od.order o " +
            "JOIN o.user s " +
            "WHERE od.classes.classId = :classId")
    List<StudentDTO> findStudentsByClassId(@Param("classId") Long classId);

    List<Class> findByCoursesCategoriesCategoryIdIn(Set<Long> categoryIds);

    List<Class> findByStatus(ClassStatus status);

    @Query("SELECT DISTINCT c FROM Class c " +
            "JOIN c.dateSlots ds " +
            "WHERE c.status = :classStatus " +
            "AND ds.date BETWEEN :startDate AND :endDate")
    List<Class> findByStatusAndStartDateBetween(@Param("classStatus") ClassStatus classStatus,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    List<Class> findByTeacherTeacherNameAndStatus(String teacherName, ClassStatus status);

    boolean existsByCode(String string);

//    boolean existsByTeacher_TeacherNameAndSlots_SlotIdAndDayOfWeekAndStartDateAndStatusNot(String teacherName, Long slotId, String dayOfWeek, LocalDate startDate, ClassStatus classStatus);

//    boolean existsByTeacher_TeacherNameAndSlots_SlotIdInAndDayOfWeekAndStartDateAndStatusNot(
//            String teacherName,
//            Set<Long> slotIds,
//            String dayOfWeek,
//            LocalDate startDate,
//            ClassStatus status);
List<Class> findByTeacher_TeacherName(String teacherName);
    List<Class> findByTeacherIsNull();
    List<Class> findByTeacherIsNotNull();
    List<Class> findByCoursesCategoriesCategoryIdInAndTeacherIsNotNull(Set<Long> categoryIds);

}
