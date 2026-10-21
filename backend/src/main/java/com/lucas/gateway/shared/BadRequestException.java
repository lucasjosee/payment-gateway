package com.lucas.gateway.shared;

// Base para as exceções em que o próprio pedido veio malformado, como um header com valor inválido.
// O ApiExceptionHandler transforma todas em 400.
public abstract class BadRequestException extends RuntimeException {

	protected BadRequestException(String message) {
		super(message);
	}
}
