package com.miniCRM.miniCRM.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Habilita @Scheduled para o job diario de notificacoes (SPEC-04 §3.1). */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
