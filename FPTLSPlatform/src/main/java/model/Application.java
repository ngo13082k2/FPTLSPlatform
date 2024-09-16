package model;

import jakarta.persistence.*;
import lombok.*;
@Builder
@Getter
@Setter
@Entity
@Table(name = "application")
@NoArgsConstructor
@AllArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long applicationId;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String title;

    private String description;

    @OneToOne
    @JoinColumn(name = "teacher_id", referencedColumnName = "teacherId")
    private Teacher teacher;
}
