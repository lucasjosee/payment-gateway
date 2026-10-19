package com.lucas.gateway.charge.fraud;

// Guardar só o motivo, sem um boolean "aprovado" ao lado, impede uma decisão contraditória,
// como aprovada e com motivo de recusa ao mesmo tempo.
public record FraudDecision(String declineReason) {

	public static FraudDecision approve() {
		return new FraudDecision(null);
	}

	public static FraudDecision decline(String reason) {
		return new FraudDecision(reason);
	}

	public boolean isApproved() {
		return declineReason == null;
	}
}
