-- Cada linha registra uma Idempotency-Key já usada e a resposta que ela gerou. Assim, um cliente que repete
-- um pedido (depois de um timeout, por exemplo) recebe a mesma resposta em vez de criar uma segunda cobrança.
CREATE TABLE idempotency_keys (
    -- A chave vale dentro de cada lojista. Dois lojistas podem usar o mesmo texto como chave sem que um
    -- esbarre no outro, nem descubra quais chaves o outro já usou.
    merchant_id     UUID         NOT NULL REFERENCES merchants (id),
    key             VARCHAR(255) NOT NULL,
    -- SHA-256 do pedido, em hexadecimal. Serve para perceber quando a mesma chave volta com um pedido diferente.
    request_hash    VARCHAR(64)  NOT NULL,
    status          VARCHAR(20)  NOT NULL CHECK (status IN ('IN_PROGRESS', 'COMPLETED')),
    response_status INTEGER,
    -- TEXT, e não JSONB, porque o JSONB reorganiza o JSON ao gravar (ordem dos campos, espaços), e a repetição
    -- precisa devolver exatamente a mesma resposta da primeira vez.
    response_body   TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    -- Depois desta data a chave deixa de valer, e um pedido com ela é tratado como novo.
    expires_at      TIMESTAMPTZ  NOT NULL,
    PRIMARY KEY (merchant_id, key),
    -- Enquanto o pedido está em andamento ainda não existe resposta. Depois de concluído, ela sempre existe.
    CONSTRAINT ck_idempotency_keys_response CHECK (
        (status = 'IN_PROGRESS' AND response_status IS NULL AND response_body IS NULL)
        OR (status = 'COMPLETED' AND response_status IS NOT NULL AND response_body IS NOT NULL)
    )
);

-- Diferente de api_keys e charges, aqui não há índice separado para merchant_id: a chave primária começa
-- por essa coluna e já serve de índice para a chave estrangeira.
