package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByAClass_ClassId(Long classId);

    List<Schedule> findBySlot_SlotId(Long slotId);
}
