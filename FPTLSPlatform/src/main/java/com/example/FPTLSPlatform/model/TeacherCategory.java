package com.example.FPTLSPlatform.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teacher_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teacher_name")
    private Teacher teacher;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}

