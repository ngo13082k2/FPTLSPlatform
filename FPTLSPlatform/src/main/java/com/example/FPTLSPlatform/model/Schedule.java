package com.example.FPTLSPlatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "schedule")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "day_of_week", nullable = false)
    private String dayOfWeek;

    @ManyToOne
    @JoinColumn(name = "slot_id", referencedColumnName = "slot_id", nullable = false)
    private Slot slot;

    @OneToMany(mappedBy = "schedule")
    private List<Class> classes;

}
