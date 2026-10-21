package com.lucas.gateway.shared;

// Base para as exceções em que o pedido está bem formado, mas a API se recusa a processá-lo do jeito que veio.
// O ApiExceptionHandler transforma todas em 422.
public abstract class UnprocessableException extends RuntimeException {

	protected UnprocessableException(String message) {
		super(message);
	}
}
