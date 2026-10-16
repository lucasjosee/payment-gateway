package com.lucas.gateway.shared;

// Base para as exceções de domínio do tipo "isso não existe". O ApiExceptionHandler transforma todas em 404.
public abstract class NotFoundException extends RuntimeException {

	protected NotFoundException(String message) {
		super(message);
	}
}
