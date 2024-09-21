package com.example.FPTLSPlatform.model;

import com.example.FPTLSPlatform.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
@Builder
@Getter
@Setter
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "phonenumber", nullable = false, unique = true)
    private String phonenumber;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "fullname", nullable = false)
    private String fullname;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @Column(name = "address")
    private String address;

    @ManyToMany(mappedBy = "users")
    private Set<Class> classes;

    @OneToOne(mappedBy = "users")
    private Teacher teacher;

    @ManyToMany(mappedBy = "users")
    private List<Rating> ratings;

    @OneToOne(mappedBy = "users", cascade = CascadeType.ALL, orphanRemoval = true)
        private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

}
