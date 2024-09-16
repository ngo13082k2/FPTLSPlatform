package model;

import jakarta.persistence.*;
import lombok.*;
@Builder
@Getter
@Setter
@Entity
@Table(name = "notification")
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String title;

    private String description;
}
