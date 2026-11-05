package com.lucas.gateway.charge;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChargeTest {

	private static final Instant DEADLINE = Instant.parse("2026-01-01T12:00:00Z");
	private static final Instant BEFORE_DEADLINE = DEADLINE.minusMillis(1);
	private static final Instant AFTER_DEADLINE = DEADLINE.plusMillis(1);

	private final Charge pix = Charge.pix(UUID.randomUUID(), 1000, "BRL", DEADLINE);

	@Test
	void pixStartsPendingWithDeadlineAndWithoutCard() {
		assertThat(pix.getStatus()).isEqualTo(ChargeStatus.PENDING);
		assertThat(pix.getPaymentMethod()).isEqualTo(PaymentMethod.PIX);
		assertThat(pix.getExpiresAt()).isEqualTo(DEADLINE);
		assertThat(pix.getCardLast4()).isNull();
		assertLastEvent(pix, null, ChargeStatus.PENDING);
	}

	@Test
	void cardChargesKeepTheirMethodAndHaveNoDeadline() {
		assertThat(authorizedCard().getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
		assertThat(authorizedCard().getExpiresAt()).isNull();

		Charge declined = Charge.declined(UUID.randomUUID(), 1000, "BRL", "0002", "fingerprint", "Cartão recusado.");
		assertThat(declined.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
		assertThat(declined.getStatus()).isEqualTo(ChargeStatus.DECLINED);
		assertThat(declined.getDeclineReason()).isEqualTo("Cartão recusado.");
	}

	@Test
	void statusesOnlyGoWhereTheStateMachineAllows() {
		assertThat(nextStatuses(ChargeStatus.PENDING))
				.containsExactlyInAnyOrder(ChargeStatus.PAID, ChargeStatus.CANCELED, ChargeStatus.EXPIRED);
		assertThat(nextStatuses(ChargeStatus.PAID)).containsExactly(ChargeStatus.REFUNDED);
		assertThat(nextStatuses(ChargeStatus.EXPIRED)).isEmpty();
		assertThat(nextStatuses(ChargeStatus.AUTHORIZED))
				.containsExactlyInAnyOrder(ChargeStatus.CAPTURED, ChargeStatus.CANCELED);
		assertThat(nextStatuses(ChargeStatus.CAPTURED)).containsExactly(ChargeStatus.REFUNDED);
	}

	@Test
	void paysBeforeTheDeadline() {
		pix.pay(BEFORE_DEADLINE);

		assertThat(pix.getStatus()).isEqualTo(ChargeStatus.PAID);
		assertLastEvent(pix, ChargeStatus.PENDING, ChargeStatus.PAID);
	}

	@ParameterizedTest
	@ValueSource(longs = { 0, 1 })
	void refusesPaymentFromTheDeadlineOnward(long millisAfterDeadline) {
		assertThatThrownBy(() -> pix.pay(DEADLINE.plusMillis(millisAfterDeadline)))
				.isInstanceOf(PixDeadlineException.class);

		assertStillPending();
	}

	@ParameterizedTest
	@ValueSource(longs = { 0, 1 })
	void expiresFromTheDeadlineOnward(long millisAfterDeadline) {
		pix.expire(DEADLINE.plusMillis(millisAfterDeadline));

		assertThat(pix.getStatus()).isEqualTo(ChargeStatus.EXPIRED);
		assertLastEvent(pix, ChargeStatus.PENDING, ChargeStatus.EXPIRED);
	}

	@Test
	void refusesExpirationBeforeTheDeadline() {
		assertThatThrownBy(() -> pix.expire(BEFORE_DEADLINE)).isInstanceOf(PixDeadlineException.class);

		assertStillPending();
	}

	@Test
	void paidPixCanBeRefunded() {
		pix.pay(BEFORE_DEADLINE);

		pix.transitionTo(ChargeStatus.REFUNDED);

		assertThat(pix.getStatus()).isEqualTo(ChargeStatus.REFUNDED);
	}

	// Os instantes estão do lado que o prazo recusaria, então o teste só passa se o status vier primeiro.
	@Test
	void pixThatLeftPendingAnswersWithTheStatusErrorInsteadOfTheDeadlineError() {
		pix.pay(BEFORE_DEADLINE);
		assertThatThrownBy(() -> pix.pay(AFTER_DEADLINE)).isInstanceOf(InvalidChargeTransitionException.class);
		assertThatThrownBy(() -> pix.expire(BEFORE_DEADLINE)).isInstanceOf(InvalidChargeTransitionException.class);

		Charge expired = Charge.pix(UUID.randomUUID(), 1000, "BRL", DEADLINE);
		expired.expire(AFTER_DEADLINE);
		assertThatThrownBy(() -> expired.pay(AFTER_DEADLINE)).isInstanceOf(InvalidChargeTransitionException.class);
	}

	// Sem prazo, conferir o prazo primeiro daria NullPointerException e viraria um 500 em vez do 409.
	@Test
	void cardChargeCannotBePaidNorExpired() {
		Charge card = authorizedCard();

		assertThatThrownBy(() -> card.pay(BEFORE_DEADLINE)).isInstanceOf(InvalidChargeTransitionException.class);
		assertThatThrownBy(() -> card.expire(AFTER_DEADLINE)).isInstanceOf(InvalidChargeTransitionException.class);
	}

	private void assertStillPending() {
		assertThat(pix.getStatus()).isEqualTo(ChargeStatus.PENDING);
		assertThat(pix.getEvents()).hasSize(1);
	}

	private static Charge authorizedCard() {
		return Charge.authorized(UUID.randomUUID(), 1000, "BRL", "1234", "fingerprint");
	}

	private static List<ChargeStatus> nextStatuses(ChargeStatus status) {
		return Arrays.stream(ChargeStatus.values()).filter(status::canTransitionTo).toList();
	}

	private static void assertLastEvent(Charge charge, ChargeStatus from, ChargeStatus to) {
		ChargeEvent last = charge.getEvents().getLast();
		assertThat(last.getFromStatus()).isEqualTo(from);
		assertThat(last.getToStatus()).isEqualTo(to);
	}
}
