package org.ntg.training.ntg_banksystem.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ntg.training.ntg_banksystem.entity.Account;
import org.ntg.training.ntg_banksystem.entity.Transaction;
import org.ntg.training.ntg_banksystem.repository.AccountRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class operationTransaction implements ItemProcessor<Transaction, Account> {

    private final AccountRepository accountRepository;

    @Override
    public Account process(Transaction transaction) throws Exception {

        int accountId = transaction.getAccountId();
        Account account = accountRepository.findByAccountId(accountId);

        if (account == null) {
            log.warn("Account with ID {} not found. Skipping transaction with ID {}", accountId, transaction.getTransactionId());
            return null;
        }

        // Debit operation
        if (transaction.getDebit() > 0) {
            if (account.getBalance() >= transaction.getDebit()) {
                account.setBalance(account.getBalance() - transaction.getDebit());
                log.info("Debited {} from account ID {}. New balance: {}", transaction.getDebit(), accountId, account.getBalance());
            } else {
                log.info("Insufficient funds in account ID {}. Transaction ID: {}", accountId, transaction.getTransactionId());
                account.setBalance(account.getBalance() - transaction.getDebit());
            }
        }

        // Credit operation
        if (transaction.getCredit() > 0) {
            account.setBalance(account.getBalance() + transaction.getCredit());
            log.info("Credited {} to account ID {}. New balance: {}", transaction.getCredit(), accountId, account.getBalance());
        }

        return account;
    }
}
