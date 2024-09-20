package com.example.FPTLSPlatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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

    @Column(name = "order_date")
    private LocalDate orderDate;

    @Column(nullable = false)
    private Double amount;

    private String status;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Transaction transaction;

    @ManyToMany
    @JoinTable(
            name = "order_class",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "class_id")
    )
    private Set<Class> classes;
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;
}
