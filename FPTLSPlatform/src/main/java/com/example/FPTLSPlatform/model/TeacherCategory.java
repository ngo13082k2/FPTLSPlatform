package com.example.FPTLSPlatform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    @JoinColumn(name = "teacher_name")
    private Teacher teacher;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "category_id")
    private Category category;
}

