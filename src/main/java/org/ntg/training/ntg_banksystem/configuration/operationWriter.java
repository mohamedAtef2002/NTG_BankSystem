package org.ntg.training.ntg_banksystem.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ntg.training.ntg_banksystem.entity.Account;
import org.ntg.training.ntg_banksystem.repository.AccountRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class operationWriter implements ItemWriter<Account> {

    private final AccountRepository accountRepository;

    @Override
    public void write(Chunk<? extends Account> accounts) throws Exception {

        accountRepository.saveAll(accounts.getItems());
        log.info("Updated balances for {} account(s).", accounts.getItems().size());
        log.info("//////////////////////////////////////////////////////////////////");
    }
}
