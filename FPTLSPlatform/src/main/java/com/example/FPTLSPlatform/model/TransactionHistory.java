package com.example.FPTLSPlatform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", nullable = false)
    private double amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "transaction_balance", nullable = false)
    private double transactionBalance;

    @ManyToOne
    @JoinColumn(name = "user_name")
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "teacher_name")
    @JsonIgnore
    private Teacher teacher;
}
