package org.ntg.training.ntg_banksystem.repository;

import org.ntg.training.ntg_banksystem.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("select t.transactionId from Transaction t")
    List<Integer> getTransactionId();

    List<Transaction> findByAccountId(int accountId);
}
