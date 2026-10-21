package com.lucas.gateway.idempotency;

import com.lucas.gateway.shared.BadRequestException;

public class InvalidIdempotencyKeyException extends BadRequestException {

	public InvalidIdempotencyKeyException(int maxLength) {
		super("A Idempotency-Key não pode ser vazia nem passar de %d caracteres.".formatted(maxLength));
	}
}
