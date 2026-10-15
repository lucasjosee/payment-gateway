package com.lucas.gateway.merchant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
}
