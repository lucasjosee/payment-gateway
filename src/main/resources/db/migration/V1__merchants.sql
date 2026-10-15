CREATE TABLE merchants (
    id         UUID         PRIMARY KEY,
    name       VARCHAR(200) NOT NULL,
    email      VARCHAR(254) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Usar lower(email) faz "Loja@Teste.com" e "loja@teste.com" contarem como o mesmo e-mail na hora de barrar duplicados.
CREATE UNIQUE INDEX ux_merchants_email ON merchants (lower(email));

CREATE TABLE api_keys (
    id          UUID        PRIMARY KEY,
    merchant_id UUID        NOT NULL REFERENCES merchants (id),
    -- Só o prefixo fica em claro, para o filtro de autenticação conseguir achar a chave.
    -- A chave completa nunca é gravada, apenas o hash SHA-256 dela em hexadecimal.
    prefix      VARCHAR(32) NOT NULL UNIQUE,
    key_hash    VARCHAR(64) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- O Postgres não cria índice para chave estrangeira sozinho. Sem este, buscar as chaves de um lojista
-- (ou apagar um lojista) obrigaria o banco a ler a tabela api_keys inteira.
CREATE INDEX ix_api_keys_merchant_id ON api_keys (merchant_id);
