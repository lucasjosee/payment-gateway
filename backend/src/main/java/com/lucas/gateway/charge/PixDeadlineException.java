package com.lucas.gateway.charge;

import com.lucas.gateway.shared.ConflictException;

// A transição existe na máquina de estados, e o que impede é o prazo. Por isso a mensagem é outra.
public class PixDeadlineException extends ConflictException {

	PixDeadlineException(String message) {
		super(message);
	}
}
