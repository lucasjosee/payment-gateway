package com.lucas.gateway.merchant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

	// A consulta é escrita à mão porque o "IgnoreCase" gerado pelo Spring Data compara com upper(),
	// e o índice único da V1 é em lower(email). Usando lower() dos dois lados, o Postgres consegue usar o índice.
	@Query("select count(m) > 0 from Merchant m where lower(m.email) = lower(:email)")
	boolean existsByEmailIgnoreCase(@Param("email") String email);
}
