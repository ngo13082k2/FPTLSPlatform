package com.example.FPTLSPlatform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @OneToOne(mappedBy = "wallet", cascade = CascadeType.ALL)
    @JsonIgnore

    private User user;
    @OneToOne(mappedBy = "wallet", cascade = CascadeType.ALL)
    @JsonIgnore

    private Teacher teacherName;
}
