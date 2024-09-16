package com.example.FPTLSPlatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
@Builder
@Getter
@Setter
@Entity
@Table(name = "wallet")
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    private Long walletId;

    @Column(name = "balance", nullable = false)
    private Double balance;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private User users;

    @OneToMany(mappedBy = "wallet")
    private Set<Transaction> transactions;
}
