package com.lucas.gateway.charge;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.CreditCardNumber;

import java.time.YearMonth;

// O RequestHasher calcula o hash da idempotência sobre este objeto convertido em JSON. O NON_NULL e o @JsonIgnore
// fazem um pedido de cartão gerar o mesmo hash de antes do Pix, e assim repetir um pedido antigo ainda devolve a
// resposta guardada.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateChargeRequest(
		@NotNull @Positive Long amountCents,
		@NotBlank @Pattern(regexp = "[A-Z]{3}", message = "deve ter 3 letras maiúsculas, como BRL") String currency,
		@Valid Card card,
		@Valid Pix pix) {

	@JsonIgnore
	@AssertTrue(message = "envie exatamente um entre card e pix")
	public boolean isExactlyOnePaymentMethod() {
		return (card == null) != (pix == null);
	}

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

	// Sem o limite de um dia, um número enorme estouraria a data do prazo e a API responderia 500.
	public record Pix(@Positive @Max(86_400) Long expiresInSeconds) {
	}
}
