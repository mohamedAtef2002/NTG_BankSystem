package org.ntg.training.ntg_banksystem.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ntg.training.ntg_banksystem.entity.Account;
import org.ntg.training.ntg_banksystem.entity.Customer;
import org.ntg.training.ntg_banksystem.repository.AccountRepository;
import org.ntg.training.ntg_banksystem.repository.CustomerRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class customerWriter implements ItemWriter<Customer> {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    @Override
    public void write(Chunk < ? extends Customer> customers ) throws Exception {
        List<Customer> customerList = (List<Customer>) customers.getItems();
        log.info("Writing {} customers to the database.", customerList.size());
        List<Customer> uniqueUsers = customerList.stream()
                .distinct()
                .filter(u -> u.getCustomerId() != 0)
                .toList();

        List<String> existsCustomer = customerRepository.getPostalCode();

        List<Customer> newCustomer = uniqueUsers.stream()
                .filter(customer -> !existsCustomer.contains(customer.getPostalCode()))
                .toList();

        if (!newCustomer.isEmpty()) {
            try {
                List<Customer> savedCustomers = customerRepository.saveAll(uniqueUsers);
                savedCustomers.forEach(customer -> {

                    customer.getAccounts().forEach(account -> {
                        account.setCustomer(customer);
                        account.setCustomerAccountId(customer.getCustomerId());
                    });
                    accountRepository.saveAll(customer.getAccounts());
                });
                log.info("Saved {} new customers to the database.", savedCustomers.size());
            } catch (Exception e) {
                log.error(e.getMessage());
                log.error("Error saving customer to the database.", e);
            }
        } else {
            log.info("No new customer to save. All provided customer already exist in the database.");
        }
    }
}
