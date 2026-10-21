package com.lucas.gateway.devlogs;

import java.time.Instant;

// Uma linha de log como chega ao navegador. O requestId vai num campo próprio para o front conseguir mostrar só as
// linhas de um pedido sem ter que recortar o texto da mensagem.
public record LogLine(Instant timestamp, String level, String logger, String requestId, String message) {
}
