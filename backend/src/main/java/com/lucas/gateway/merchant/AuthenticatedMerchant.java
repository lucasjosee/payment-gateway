package com.lucas.gateway.merchant;

import java.util.UUID;

// É isto que um controller recebe com @AuthenticationPrincipal depois que o filtro de API key do pacote auth
// reconhece a chave: só o id do lojista dono dela.
public record AuthenticatedMerchant(UUID id) {
}
