package com.lucas.gateway.charge;

import com.lucas.gateway.shared.ConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class PixExpirationJob {

	private static final Logger log = LoggerFactory.getLogger(PixExpirationJob.class);

	private final ChargeRepository chargeRepository;
	private final ChargeService chargeService;

	public PixExpirationJob(ChargeRepository chargeRepository, ChargeService chargeService) {
		this.chargeRepository = chargeRepository;
		this.chargeService = chargeService;
	}

	// A transação fica no ChargeService, e não aqui, para cada Pix ter a sua: um conflito desfaz só aquela expiração.
	@Scheduled(fixedDelay = 10_000)
	public void expireOverdue() {
		for (UUID chargeId : chargeRepository.findOverduePixIds(Instant.now())) {
			try {
				chargeService.expirePix(chargeId);
			} catch (ObjectOptimisticLockingFailureException | ConflictException e) {
				// Um pagamento ou cancelamento chegou primeiro. Se foi antes de o Pix ser carregado, quem recusa é a
				// máquina de estados, e se foi depois, é o lock otimista, na hora do commit.
				log.info("A cobrança {} mudou de status antes de expirar, e vale o que chegou primeiro", chargeId);
			}
		}
	}
}
