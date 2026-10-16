package com.lucas.gateway.shared;

// Base para as exceções de domínio em que o pedido é válido, mas choca com o estado atual dos dados.
// O ApiExceptionHandler transforma todas em 409.
public abstract class ConflictException extends RuntimeException {

	protected ConflictException(String message) {
		super(message);
	}
}
