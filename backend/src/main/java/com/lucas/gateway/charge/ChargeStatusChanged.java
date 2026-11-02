package com.lucas.gateway.charge;

import java.util.UUID;

// Publicado pelo ChargeService quando uma cobrança nasce e a cada mudança de status. O charge não sabe quem
// escuta, e é assim que o pacote webhook fica sabendo das mudanças sem o charge depender dele.
public record ChargeStatusChanged(UUID merchantId, ChargeResponse charge) {
}
