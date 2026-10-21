package com.lucas.gateway.charge;

import java.util.List;

// Formato próprio, em vez de devolver o Page do Spring Data direto: o JSON do Page é um detalhe interno do
// framework e pode mudar numa atualização, e esta resposta é um contrato com o front.
public record ChargePage(List<ChargeSummary> items, int page, int size, long totalItems, int totalPages) {
}
