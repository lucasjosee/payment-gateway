-- O DEFAULT dá valor às cobranças que já existem e também às novas, enquanto a entidade não conhece esse campo.
ALTER TABLE charges
    ADD COLUMN payment_method VARCHAR(20) NOT NULL DEFAULT 'CARD' CHECK (payment_method IN ('CARD', 'PIX')),
    ADD COLUMN expires_at     TIMESTAMPTZ,
    ALTER COLUMN card_last4       DROP NOT NULL,
    ALTER COLUMN card_fingerprint DROP NOT NULL,
    -- O prazo entra junto com os campos do cartão porque um Pix sem prazo nunca expiraria.
    ADD CONSTRAINT ck_charges_payment_method_fields CHECK (
        (payment_method = 'CARD' AND card_last4 IS NOT NULL AND card_fingerprint IS NOT NULL AND expires_at IS NULL)
        OR (payment_method = 'PIX' AND card_last4 IS NULL AND card_fingerprint IS NULL AND expires_at IS NOT NULL)
    );

-- A V2 criou estas três constraints sem nome, e estes são os nomes que o Postgres deu a elas.
ALTER TABLE charges
    DROP CONSTRAINT charges_status_check,
    ADD CONSTRAINT charges_status_check CHECK (status IN (
        'AUTHORIZED', 'DECLINED', 'CAPTURED', 'CANCELED', 'REFUNDED', 'PENDING', 'PAID', 'EXPIRED'
    ));

ALTER TABLE charge_events
    DROP CONSTRAINT charge_events_from_status_check,
    ADD CONSTRAINT charge_events_from_status_check CHECK (from_status IN (
        'AUTHORIZED', 'DECLINED', 'CAPTURED', 'CANCELED', 'REFUNDED', 'PENDING', 'PAID', 'EXPIRED'
    )),
    DROP CONSTRAINT charge_events_to_status_check,
    ADD CONSTRAINT charge_events_to_status_check CHECK (to_status IN (
        'AUTHORIZED', 'DECLINED', 'CAPTURED', 'CANCELED', 'REFUNDED', 'PENDING', 'PAID', 'EXPIRED'
    ));

-- Só os Pix pendentes podem expirar. As demais cobranças, que serão a maioria, ficam fora do índice.
CREATE INDEX ix_charges_pending_expires_at ON charges (expires_at) WHERE status = 'PENDING';
