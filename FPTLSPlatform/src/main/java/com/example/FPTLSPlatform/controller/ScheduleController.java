package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.ScheduleDTO;
import com.example.FPTLSPlatform.service.IScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    @Autowired
    private IScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<ScheduleDTO> createSchedule(@RequestBody ScheduleDTO scheduleDto) {
        try {
            ScheduleDTO createdSchedule = scheduleService.createSchedule(scheduleDto);
            return new ResponseEntity<>(createdSchedule, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleDTO> updateSchedule(@PathVariable Long scheduleId, @RequestBody ScheduleDTO scheduleDto) {
        try {
            ScheduleDTO updatedSchedule = scheduleService.updateSchedule(scheduleId, scheduleDto);
            return new ResponseEntity<>(updatedSchedule, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleRuntimeException(RuntimeException e) {
        return e.getMessage();
    }

    @GetMapping
    public ResponseEntity<List<ScheduleDTO>> getAllSchedules() {
        List<ScheduleDTO> schedules = scheduleService.getAllSchedules();
        return new ResponseEntity<>(schedules, HttpStatus.OK);
    }

    @GetMapping("/{scheduleId}")
    public ResponseEntity<ScheduleDTO> getScheduleById(@PathVariable Long scheduleId) {
        try {
            ScheduleDTO schedule = scheduleService.getScheduleById(scheduleId);
            return new ResponseEntity<>(schedule, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<ScheduleDTO> getSchedulesByClassId(@PathVariable Long classId) {
        ScheduleDTO schedules = scheduleService.getSchedulesByClassId(classId);
        return new ResponseEntity<>(schedules, HttpStatus.OK);
    }

    @GetMapping("/slot/{slotId}")
    public ResponseEntity<List<ScheduleDTO>> getSchedulesBySlotId(@PathVariable Long slotId) {
        List<ScheduleDTO> schedules = scheduleService.getSchedulesBySlotId(slotId);
        return new ResponseEntity<>(schedules, HttpStatus.OK);
    }
}
