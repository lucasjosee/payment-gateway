package com.lucas.gateway.charge;

import com.lucas.gateway.shared.NotFoundException;

public class ChargeNotFoundException extends NotFoundException {

	public ChargeNotFoundException() {
		super("Cobrança não encontrada.");
	}
}
