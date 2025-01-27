package org.ntg.training.ntg_banksystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@RequiredArgsConstructor
@Setter
@Getter
@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int transactionId;
    private String description;
    private double credit;
    private double debit;
    private double timestamp;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

}
