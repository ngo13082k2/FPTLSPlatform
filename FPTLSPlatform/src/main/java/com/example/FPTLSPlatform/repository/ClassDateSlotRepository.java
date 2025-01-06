package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.Certificate;
import com.example.FPTLSPlatform.model.ClassDateSlot;
import com.example.FPTLSPlatform.model.enums.ClassStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ClassDateSlotRepository extends JpaRepository<ClassDateSlot, Long> {
    boolean existsByClazz_Teacher_TeacherNameAndDateAndSlot_SlotIdAndClazz_StatusNot(
            String teacherName,
            LocalDate date,
            Long slotId,
            ClassStatus status);
}
