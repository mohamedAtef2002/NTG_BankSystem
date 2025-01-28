package org.ntg.training.ntg_banksystem.configuration;

import lombok.RequiredArgsConstructor;
import org.ntg.training.ntg_banksystem.entity.Transaction;
import org.ntg.training.ntg_banksystem.repository.TransactionRepository;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class opeartionTransationReader implements ItemReader<Transaction> {

    private final TransactionRepository transactionRepository;
    private Iterator<Transaction> transactionIterator;

    @Override
    public Transaction read() {
        if (transactionIterator == null) {
            List<Transaction> transactions = transactionRepository.findAll();
            transactionIterator = transactions.iterator();
        }

        return transactionIterator.hasNext() ? transactionIterator.next() : null;
    }
}
