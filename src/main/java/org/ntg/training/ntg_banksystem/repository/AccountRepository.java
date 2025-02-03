package org.ntg.training.ntg_banksystem.repository;

import org.ntg.training.ntg_banksystem.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByAccountId(int accountId);

    List<Account> findByCustomerId(Long customer_id);
    List<Account> findByCustomerAccountId(int customer_id);

}
