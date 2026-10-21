package com.lucas.gateway.idempotency;

import com.lucas.gateway.shared.UnprocessableException;

public class IdempotencyKeyReusedException extends UnprocessableException {

	public IdempotencyKeyReusedException() {
		super("Esta Idempotency-Key já foi usada com um pedido diferente. Para um pedido novo, use uma chave nova.");
	}
}
