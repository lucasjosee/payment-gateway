package com.lucas.gateway.merchant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMerchantRequest(
		@NotBlank @Size(max = 200) String name,
		@NotBlank @Email @Size(max = 254) String email) {
}
