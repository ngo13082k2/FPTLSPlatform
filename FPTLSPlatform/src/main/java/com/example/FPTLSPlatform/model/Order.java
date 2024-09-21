package com.example.FPTLSPlatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Builder
@Getter
@Setter
@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "username", referencedColumnName = "user_name", nullable = false)
    private User users;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    @Column(name = "status", nullable = false)
    private String status;
}
