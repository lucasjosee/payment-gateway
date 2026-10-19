package com.lucas.gateway.charge;

import com.lucas.gateway.shared.ConflictException;

public class InvalidChargeTransitionException extends ConflictException {

	public InvalidChargeTransitionException(ChargeStatus from, ChargeStatus to) {
		super("Uma cobrança com status %s não pode passar para %s.".formatted(from, to));
	}
}
