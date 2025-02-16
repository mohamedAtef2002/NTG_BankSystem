package org.ntg.training.ntg_banksystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
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
    private LocalDateTime timestamp;
    private int accountId;

    @ManyToOne
    private Account account;

    public Transaction(Transaction transaction) {
        this.transactionId = transaction.transactionId;
        this.description = transaction.description;
        this.credit = transaction.credit;
        this.debit = transaction.debit;
        this.timestamp = transaction.timestamp;
        this.accountId = transaction.accountId;
        this.account = transaction.account;
    }

}
