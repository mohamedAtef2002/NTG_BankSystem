package org.ntg.training.ntg_banksystem.configuration;

import lombok.extern.slf4j.Slf4j;
import org.ntg.training.ntg_banksystem.entity.Account;
import org.ntg.training.ntg_banksystem.entity.Transaction;
import org.ntg.training.ntg_banksystem.repository.AccountRepository;
import org.ntg.training.ntg_banksystem.repository.TransactionRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class transactionWriter implements ItemWriter<Transaction> {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public transactionWriter(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public void write(Chunk<? extends Transaction> transactions) throws Exception {
        List<Transaction> transactionList = (List<Transaction>) transactions.getItems();

        if (transactionList.isEmpty()) {
            return;
        }


        Map<Integer, Account> accountMap = transactionList.stream()
                .map(Transaction::getAccountId)
                .distinct()
                .collect(Collectors.toMap(
                        accountId -> accountId,
                        accountId -> accountRepository.findByAccountId(accountId),
                        (existing, replacement) -> existing
                ));

        transactionList.forEach(transaction -> {
            Account account = accountMap.get(transaction.getAccountId());
            if (account != null) {
                transaction.setAccount(account);
            }
        });

        Set<Integer> existingTransactionIds = new HashSet<>(transactionRepository.getTransactionId());

        List<Transaction> newTransactions = transactionList.stream()
                .filter(transaction -> !existingTransactionIds.contains(transaction.getTransactionId()))
                .toList();

        if (!newTransactions.isEmpty()) {
            try {
                transactionRepository.saveAll(newTransactions);
                log.info("Saved {} new transaction(s) to the database.", newTransactions.size());
            } catch (Exception e) {
                log.error("Error saving transactions to the database: {}", e.getMessage());
            }
        }
    }
}
