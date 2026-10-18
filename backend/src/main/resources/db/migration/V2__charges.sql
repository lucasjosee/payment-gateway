CREATE TABLE charges (
    id               UUID         PRIMARY KEY,
    merchant_id      UUID         NOT NULL REFERENCES merchants (id),
    amount_cents     BIGINT       NOT NULL CHECK (amount_cents > 0),
    currency         VARCHAR(3)   NOT NULL,
    -- A mesma lista de estados se repete em charge_events e no enum da aplicação.
    -- Um estado novo precisa de uma migration que altere as três constraints, além do enum.
    status           VARCHAR(20)  NOT NULL CHECK (status IN ('AUTHORIZED', 'DECLINED', 'CAPTURED', 'CANCELED', 'REFUNDED')),
    card_last4       VARCHAR(4)   NOT NULL,
    -- HMAC-SHA256 do número do cartão, calculado com uma chave secreta da aplicação. Serve para saber que duas
    -- cobranças usaram o mesmo cartão sem guardar o número. Um SHA-256 sem chave não protegeria nada: conhecendo
    -- o BIN e os 4 últimos dígitos, sobram tão poucas combinações que daria para testar todas e achar o número.
    card_fingerprint VARCHAR(64)  NOT NULL,
    decline_reason   VARCHAR(200),
    version          BIGINT       NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    -- Uma cobrança recusada sempre tem o motivo da recusa, e nenhuma outra tem motivo.
    CONSTRAINT ck_charges_decline_reason CHECK ((status = 'DECLINED') = (decline_reason IS NOT NULL))
);

-- Mesmo motivo do índice em api_keys: o Postgres não cria índice para chave estrangeira sozinho.
CREATE INDEX ix_charges_merchant_id ON charges (merchant_id);

-- O antifraude conta quantas cobranças o mesmo cartão teve nos últimos minutos. Com o fingerprint primeiro
-- e a data depois, o banco vai direto às cobranças daquele cartão e lê só as mais recentes.
CREATE INDEX ix_charges_card_fingerprint_created_at ON charges (card_fingerprint, created_at);

CREATE TABLE charge_events (
    id          UUID        PRIMARY KEY,
    charge_id   UUID        NOT NULL REFERENCES charges (id),
    -- Fica vazio no primeiro evento, porque a cobrança já nasce autorizada ou recusada.
    from_status VARCHAR(20) CHECK (from_status IN ('AUTHORIZED', 'DECLINED', 'CAPTURED', 'CANCELED', 'REFUNDED')),
    to_status   VARCHAR(20) NOT NULL CHECK (to_status IN ('AUTHORIZED', 'DECLINED', 'CAPTURED', 'CANCELED', 'REFUNDED')),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX ix_charge_events_charge_id ON charge_events (charge_id);
