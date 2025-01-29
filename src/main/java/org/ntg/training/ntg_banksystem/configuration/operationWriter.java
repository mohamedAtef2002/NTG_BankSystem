package org.ntg.training.ntg_banksystem.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ntg.training.ntg_banksystem.entity.Account;
import org.ntg.training.ntg_banksystem.entity.Customer;
import org.ntg.training.ntg_banksystem.entity.Transaction;
import org.ntg.training.ntg_banksystem.repository.AccountRepository;
import org.ntg.training.ntg_banksystem.repository.CustomerRepository;
import org.ntg.training.ntg_banksystem.repository.TransactionRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;



@Component
@RequiredArgsConstructor
@Slf4j
public class operationWriter implements ItemWriter<Account> {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public void write(Chunk<? extends Account> accounts) throws Exception {

        accountRepository.saveAll(accounts.getItems());
        log.info("Updated balances for {} account(s).", accounts.getItems().size());
        log.info("//////////////////////////////////////////////////////////////////");

        //print file to customer
        List<Customer> customers = customerRepository.findAll();

        for (Customer customer : customers) {
            StringBuilder content = new StringBuilder();
            List<Account> accounts1 = accountRepository.findByCustomerId(customer.getId());

            content.append(String.format("%-20s%60s%n",
                    customer.getFirstName() + " " + customer.getLastName(),
                    "NTG Bank"));
            content.append(String.format("%-20s%70s%n",
                    customer.getAddress1(),
                    "1060 West Addison St."));
            content.append(String.format("%-20s%60s%n",
                    customer.getCity() + ", " + customer.getState() + " " + customer.getPostalCode(),
                    "Chicago, IL 60613"));
            content.append("\n");

            for (Account account : accounts1) {
                content.append("Your Account Summary\n");
                content.append(String.format("%-20s%60s%n",
                        "Statement Period: ", "Start Date - End Date"));
                content.append("\n");

                List<Transaction> transactionList = transactionRepository.findByAccountId(account.getAccountId());

                for (Transaction transaction : transactionList) {
                    content.append(String.format("%-15s%-30s%-20.2f%n",
                            transaction.getTimestamp(),
                            "          "+transaction.getDescription(),
                            transaction.getDebit() > 0 ? transaction.getDebit() : transaction.getCredit()));
                }

                // Totals
                double totalDebit = transactionList.stream()
                        .mapToDouble(Transaction::getDebit)
                        .sum();
                double totalCredit = transactionList.stream()
                        .mapToDouble(Transaction::getCredit)
                        .sum();

                content.append("\n");
                content.append(String.format("%-20s%50f%n", "Total Debit: ", totalDebit));
                content.append(String.format("%-20s%50f%n", "Total Credit: ", totalCredit));
                content.append(String.format("%-20s%50f%n", "Balance: ", account.getBalance()));
                content.append("\n");
            }
            writeToFile(customer.getFirstName() + customer.getMiddleName() + customer.getLastName() + "_statement.txt", content.toString());
        }
    }
    private void writeToFile(String fileName, String content) {
        try {
            String folderPath = "D:/NTG_BankSystem/src/main/resources/customerTXT";
            Path folder = Path.of(folderPath);
            Files.createDirectories(folder);
            String filePath = folderPath + "/" + fileName;
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(content);
            }

            System.out.println("File written successfully to: " + filePath);

        } catch (IOException e) {
            System.err.println("An error occurred while writing to file: " + e.getMessage());
        }
    }
}
