package com.miniCRM.miniCRM.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;

@Slf4j
public abstract class SeedBase implements CommandLineRunner {
    @Override public final void run(String... args) {   // o template: fixo, final
        if (jaExecutou()) return;
        criarDados();
        log.info("Seed {} aplicado", nome());
    }
    protected abstract boolean jaExecutou();            // passos que variam
    protected abstract void criarDados();
    protected abstract String nome();
}