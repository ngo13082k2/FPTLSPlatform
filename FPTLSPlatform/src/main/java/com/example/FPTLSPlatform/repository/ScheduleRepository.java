package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByClasses_ClassId(Long classId);

    List<Schedule> findBySlot_SlotId(Long slotId);
    Optional<Schedule> findByDayOfWeekAndSlot_SlotId(String dayOfWeek, Long slotId);


}
