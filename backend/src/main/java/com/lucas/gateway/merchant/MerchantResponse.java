package com.lucas.gateway.merchant;

import java.util.UUID;

public record MerchantResponse(UUID id, String name, String email) {
}
