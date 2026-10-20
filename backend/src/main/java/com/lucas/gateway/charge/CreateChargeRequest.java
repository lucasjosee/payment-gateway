package com.lucas.gateway.charge;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.CreditCardNumber;

import java.time.YearMonth;

public record CreateChargeRequest(
		@NotNull @Positive Long amountCents,
		@NotBlank @Pattern(regexp = "[A-Z]{3}", message = "deve ter 3 letras maiúsculas, como BRL") String currency,
		@NotNull @Valid Card card) {

	// O CVV só é validado e morre aqui: o PCI DSS proíbe guardá-lo depois da autorização, mesmo criptografado.
	// A validade usa @FutureOrPresent porque um cartão que vence neste mês ainda vale até o último dia dele.
	public record Card(
			@NotBlank @Pattern(regexp = "\\d{13,19}", message = "deve ter de 13 a 19 dígitos, sem espaços")
			@CreditCardNumber String number,
			@NotNull @FutureOrPresent(message = "o cartão está vencido") YearMonth expiry,
			@NotBlank @Pattern(regexp = "\\d{3,4}", message = "deve ter 3 ou 4 dígitos") String cvv) {

		// O toString padrão de um record imprime todos os campos, e um log deste objeto exporia o número e o CVV.
		@Override
		public String toString() {
			return "Card[number=****, expiry=%s]".formatted(expiry);
		}
	}
}
