package com.example.FPTLSPlatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@Entity
@Table(name = "courses")
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @Column(name = "course_code", nullable = false, unique = true)
    private String courseCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status")
    private String status;

    @Column(name = "image")
    private String image;

//    @Column(name = "duration", nullable = false)
//    private int duration;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "category_id", nullable = false)
    private Category categories;

    @OneToOne(mappedBy = "course", cascade = CascadeType.ALL)
    private Document document;
}
