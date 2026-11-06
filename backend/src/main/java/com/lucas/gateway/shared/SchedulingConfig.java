package com.lucas.gateway.shared;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// Liga o agendamento do WebhookDispatcher e do PixExpirationJob. Os testes desligam a propriedade em
// src/test/resources/config/application.yaml: os dois continuam existindo, mas só rodam quando um teste os chama.
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "gateway.scheduling.enabled", matchIfMissing = true)
public class SchedulingConfig {
}
