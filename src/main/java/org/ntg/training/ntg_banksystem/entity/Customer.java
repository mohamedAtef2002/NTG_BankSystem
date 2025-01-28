package org.ntg.training.ntg_banksystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customerId")
    private int customerId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String postalCode;
    private String emailAddress;
    private String homePhone;
    private String cellPhone;
    private String workPhone;


    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Account> accounts;

    public Customer(Customer customer) {
        this.customerId = customer.customerId;
        this.firstName = customer.firstName;
        this.middleName = customer.middleName;
        this.lastName = customer.lastName;
        this.address1 = customer.address1;
        this.city = customer.city;
        this.address2 = customer.address2;
        this.state = customer.state;
        this.postalCode = customer.postalCode;
        this.emailAddress = customer.emailAddress;
        this.homePhone = customer.homePhone;
        this.cellPhone = customer.cellPhone;
        this.workPhone = customer.workPhone;
        this.accounts = new ArrayList<>();
        for (Account account : customer.accounts) {
            this.accounts.add(new Account(account));
        }
    }
}
