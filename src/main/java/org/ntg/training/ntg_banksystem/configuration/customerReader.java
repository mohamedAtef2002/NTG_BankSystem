package org.ntg.training.ntg_banksystem.configuration;

import jakarta.annotation.PostConstruct;
import org.ntg.training.ntg_banksystem.entity.Account;
import org.ntg.training.ntg_banksystem.entity.Customer;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component
public class customerReader implements ItemReader<Customer> {

    private List<Customer> customers;
    private int currentIndex = 0;

    @PostConstruct
    public void init() throws IOException {
        customers = new ArrayList<>();
        Path path = Paths.get("src/main/resources/customerData.csv");

        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(line -> {
                String[] split = line.split(",");
                Customer customer = new Customer();
                customer.setCustomerId(Integer.parseInt(split[0].replaceAll("[^\\d]", "").trim()));
                customer.setFirstName(split[2]);
                customer.setMiddleName(split[3]);
                customer.setLastName(split[4]);
                customer.setAddress1(split[5]);
                customer.setAddress2(split[6]);
                customer.setCity(split[7]);
                customer.setState(split[8]);
                customer.setPostalCode(split[9]);
                customer.setEmailAddress(split[10]);
                customer.setHomePhone(split[11]);
                customer.setCellPhone(split[12]);
                customer.setWorkPhone(split[13]);

                Account account = new Account();
                account.setAccountId(Integer.parseInt(split[1]));
                account.setBalance(0.00);
                account.setCustomer(customer);

                customer.setAccounts(new ArrayList<>());
                customer.getAccounts().add(account);

                customers.add(customer);
            });
        }
    }

    @Override
    public Customer read() {
        if (currentIndex < customers.size()) {
            Customer customer = customers.get(currentIndex);
            currentIndex++;
            return new Customer(customer);
        } else {
            currentIndex = 0;
            return null;
        }
    }
}
