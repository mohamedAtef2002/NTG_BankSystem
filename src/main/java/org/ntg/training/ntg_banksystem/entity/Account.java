package org.ntg.training.ntg_banksystem.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long unId;

    private int accountId;
    private Double balance;
    private int customerAccountId;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Transaction> transactions = new ArrayList<>();

    public Account(Account account) {
        this.accountId = account.accountId;
        this.balance = account.balance;


        this.transactions = new ArrayList<>();
        for (Transaction transaction : account.transactions) {
            this.transactions.add(new Transaction(transaction));
        }
    }

    public void addTransaction(Transaction transaction) {
        transaction.setAccount(this);
        this.transactions.add(transaction);
    }
}
