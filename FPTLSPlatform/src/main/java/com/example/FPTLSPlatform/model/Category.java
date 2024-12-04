package com.example.FPTLSPlatform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@Entity
@Table(name = "category")
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "name", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String name;

    @ManyToMany(mappedBy = "major")
    @JsonIgnore
    private List<User> users;

    @ManyToMany(mappedBy = "major")
    @JsonIgnore
    private List<Teacher> teachers;

}
