package org.ntg.training.ntg_banksystem.configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.ntg.training.ntg_banksystem.entity.Account;
import org.ntg.training.ntg_banksystem.entity.Transaction;
import org.ntg.training.ntg_banksystem.repository.AccountRepository;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class transactionReader implements ItemReader<Transaction> {

    private final AccountRepository accountRepository;
    private List<Transaction> transactions;
    private int currentIndex = 0;

    @PostConstruct
    public void init() throws IOException {
        transactions = new ArrayList<>();
        Path path = Paths.get("src/main/resources/transactionData.csv");

        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(line -> {
                String[] split = line.split(",");
                Transaction transaction = new Transaction();
                transaction.setTransactionId(Integer.parseInt(split[0].replaceAll("[^\\d]", "").trim()));
                transaction.setDescription(split[2]);
                try {
                    transaction.setCredit(parseDoubleSafely(split[3]));
                } catch (NumberFormatException e) {
                    transaction.setCredit(0.0);
                }

                try {
                    transaction.setDebit(parseDoubleSafely(split[4]));
                } catch (NumberFormatException e) {
                    transaction.setDebit(0.0);
                }

                try {
                    String timestamp = split[5].replaceAll("['\"]", "").trim();
                    timestamp = timestamp.split("\\.")[0];

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    transaction.setTimestamp(LocalDateTime.parse(timestamp, formatter));
                } catch (Exception e) {
                    System.out.println("Error parsing timestamp: " + e.getMessage());
                    transaction.setTimestamp(null);
                }

                int accountId = Integer.parseInt(split[1].trim());
                transaction.setAccountId(accountId);
                transactions.add(transaction);
            });
        }
    }


    @Override
    public Transaction read() {
        if (currentIndex < transactions.size()) {
            Transaction transaction = transactions.get(currentIndex);
            currentIndex++;
            return new Transaction(transaction);
        } else {
            currentIndex = 0;
            return null;
        }
    }

    private double parseDoubleSafely(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.replaceAll("[^\\d.]", "").trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}

