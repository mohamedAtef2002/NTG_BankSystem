package org.ntg.training.ntg_banksystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NtgBankSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(NtgBankSystemApplication.class, args);
    }

}
