package com.example.FPTLSPlatform.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "system_wallet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount = 0.0;

    public void addAmount(Double amount) {
        this.totalAmount += amount;
    }
}
