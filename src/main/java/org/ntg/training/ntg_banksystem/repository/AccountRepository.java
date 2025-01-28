package org.ntg.training.ntg_banksystem.repository;

import org.ntg.training.ntg_banksystem.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByAccountId(int accountId);
}
