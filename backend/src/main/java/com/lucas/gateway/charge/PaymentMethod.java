package com.lucas.gateway.charge;

// Os nomes se repetem nas CHECK constraints da V7__pix.sql, que amarram os campos da cobrança a cada meio de pagamento.
public enum PaymentMethod {

	CARD,
	PIX
}
