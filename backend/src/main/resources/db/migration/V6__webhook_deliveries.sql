CREATE TABLE webhook_deliveries (
    id               UUID        PRIMARY KEY,
    merchant_id      UUID        NOT NULL REFERENCES merchants (id),
    charge_id        UUID        NOT NULL REFERENCES charges (id),
    -- Sem CHECK de propósito: há um tipo por estado da cobrança, e uma lista aqui seria a quarta cópia desses estados.
    event_type       VARCHAR(50) NOT NULL,
    -- TEXT, e não JSONB, porque o JSONB reorganiza o JSON ao gravar, e cada tentativa precisa mandar os mesmos bytes.
    payload          TEXT        NOT NULL,
    status           VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'DELIVERED', 'FAILED')),
    attempts         INTEGER     NOT NULL DEFAULT 0 CHECK (attempts >= 0),
    next_attempt_at  TIMESTAMPTZ,
    -- Fica vazio antes da primeira tentativa e quando o envio falha sem resposta, como num timeout.
    last_http_status INTEGER,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- Só uma entrega pendente tem próxima tentativa.
    CONSTRAINT ck_webhook_deliveries_next_attempt CHECK ((status = 'PENDING') = (next_attempt_at IS NOT NULL))
);

-- O despachante só procura entregas pendentes. As entregues e as que falharam, que serão a maioria, ficam fora do
-- índice, e ele continua pequeno mesmo com a tabela crescendo.
CREATE INDEX ix_webhook_deliveries_pending_next_attempt_at ON webhook_deliveries (next_attempt_at)
    WHERE status = 'PENDING';

-- Atende a chave estrangeira e a listagem das entregas do lojista, como o índice da V4 faz em charges.
CREATE INDEX ix_webhook_deliveries_merchant_id_created_at ON webhook_deliveries (merchant_id, created_at);
CREATE INDEX ix_webhook_deliveries_charge_id ON webhook_deliveries (charge_id);
