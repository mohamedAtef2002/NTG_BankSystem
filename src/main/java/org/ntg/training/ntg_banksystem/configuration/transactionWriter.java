package org.ntg.training.ntg_banksystem.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ntg.training.ntg_banksystem.entity.Transaction;
import org.ntg.training.ntg_banksystem.repository.TransactionRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class transactionWriter implements ItemWriter<Transaction> {

    private final TransactionRepository transactionRepository;

    @Override
    public void write(Chunk<? extends Transaction> transactions) throws Exception {
        List<Transaction> transactionList = (List<Transaction>) transactions.getItems();
        log.info("Writing {} transaction to the database.", transactionList.size());
        List<Transaction> uniqueTransaction = transactionList.stream()
                .distinct()
                .filter(u -> u.getTransactionId() != 0)
                .toList();

        List<Integer> existsTraInteger = transactionRepository.getTransactionId();

        List<Transaction> newTransaction = uniqueTransaction.stream()
                .filter(customer -> !existsTraInteger.contains(customer.getTransactionId()))
                .toList();

        if (!newTransaction.isEmpty()) {
            try {
                transactionRepository.saveAll(newTransaction);
                log.info("Saved {} new transaction to the database.", newTransaction.size());
            }catch (Exception e){
                log.error(e.getMessage());
                log.error("Error saving transaction to the database.", e);
            }
        }else {
            log.info("No new transaction to save. All provided transaction already exist in the database.");
        }
    }
}
