package com.lucas.gateway.idempotency;

import com.lucas.gateway.shared.ConflictException;

public class IdempotencyKeyInProgressException extends ConflictException {

	public IdempotencyKeyInProgressException() {
		super("Um pedido com esta Idempotency-Key ainda está em andamento. Tente de novo em instantes.");
	}
}
