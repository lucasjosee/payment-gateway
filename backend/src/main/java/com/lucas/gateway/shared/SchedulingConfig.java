package com.lucas.gateway.shared;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// Liga o agendamento que o WebhookDispatcher usa para procurar entregas a cada segundo. Os testes desligam a
// propriedade em src/test/resources/config/application.yaml: o despachante continua existindo, mas só roda quando
// um teste o chama.
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "gateway.scheduling.enabled", matchIfMissing = true)
public class SchedulingConfig {
}
