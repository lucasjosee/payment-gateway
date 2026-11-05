package com.lucas.gateway.charge;

import com.lucas.gateway.shared.UnprocessableException;

// O cartão em outra moeda vira uma cobrança DECLINED pelo antifraude. O Pix não passa por ele nem tem esse status,
// então o pedido é recusado antes de a cobrança existir.
public class PixCurrencyNotSupportedException extends UnprocessableException {

	PixCurrencyNotSupportedException() {
		super("Pix só aceita a moeda BRL.");
	}
}
