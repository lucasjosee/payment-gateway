-- A listagem filtra pelo lojista e ordena da cobrança mais recente para a mais antiga. Com o lojista primeiro e a
-- data depois, o banco encontra as cobranças daquele lojista já em ordem e lê só as da página pedida. O índice é
-- crescente, mas o Postgres consegue percorrê-lo de trás para frente, então ele serve igual para a ordem decrescente.
CREATE INDEX ix_charges_merchant_id_created_at ON charges (merchant_id, created_at);

-- O índice novo começa por merchant_id e também atende a chave estrangeira, então o antigo ficou sobrando.
DROP INDEX ix_charges_merchant_id;
