package org.ntg.training.ntg_banksystem.repository;

import org.ntg.training.ntg_banksystem.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query("select c.customerId from Customer c")
    List<Integer> getCustomerId();

    @Query("select c.postalCode from  Customer c")
    List<String> getPostalCode();
}
