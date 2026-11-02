CREATE TABLE webhook_endpoints (
    id          UUID          PRIMARY KEY,
    -- Um endpoint por lojista. O UNIQUE garante isso mesmo que dois cadastros cheguem juntos, e também serve de
    -- índice para a chave estrangeira.
    merchant_id UUID          NOT NULL UNIQUE REFERENCES merchants (id),
    url         VARCHAR(2048) NOT NULL,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);
