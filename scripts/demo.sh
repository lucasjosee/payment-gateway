#!/usr/bin/env bash
# Roda o roteiro da demo contra uma instância da API e para no primeiro passo que não der o resultado esperado.
# Assim o mesmo script serve para apresentar e para conferir se um deploy está funcionando.
#
# Uso: ./scripts/demo.sh http://localhost:8080/api
set -euo pipefail

if [[ $# -ne 1 ]]; then
	echo "Uso: $0 <url-da-api>, por exemplo: $0 http://localhost:8080/api" >&2
	exit 1
fi
BASE_URL=${1%/}

# Cartão de teste que a API sempre recusa, pela regra do final 0002.
DECLINED_CARD=4000000000000002

STATUS=
BODY=

step() {
	printf '\n%s\n' "$1"
}

# Mostra a linha "MÉTODO rota -> código" e guarda o código em STATUS e o corpo em BODY. Os argumentos depois do
# corpo vão direto para o curl, como os headers de autenticação e de idempotência.
request() {
	local method=$1 path=$2 body=$3
	shift 3
	local args=(-s -X "$method" -w '\n%{http_code}' "$@")
	if [[ -n $body ]]; then
		args+=(-H 'Content-Type: application/json' -d "$body")
	fi

	local response
	response=$(curl "${args[@]}" "$BASE_URL$path") || {
		echo "   Sem resposta de $BASE_URL$path." >&2
		exit 1
	}
	BODY=${response%$'\n'*}
	STATUS=${response##*$'\n'}
	printf '   %s %s -> %s\n' "$method" "$path" "$STATUS"
}

expect_status() {
	if [[ $STATUS != "$1" ]]; then
		printf '   Esperava %s. Resposta recebida:\n   %s\n' "$1" "$BODY" >&2
		exit 1
	fi
}

# Lê um campo de texto da resposta sem depender do jq. Funciona aqui porque, nas respostas da API, cada campo
# lido pelo script aparece uma vez só e nenhum valor tem aspas dentro.
field() {
	sed -n "s/.*\"$1\":\"\([^\"]*\)\".*/\1/p" <<< "$BODY"
}

state_history() {
	grep -o '"toStatus":"[A-Z]*"' <<< "$BODY" | cut -d'"' -f4 | paste -sd ' ' - | sed 's/ / -> /g'
}

# A regra de velocidade do antifraude recusa um cartão a partir da 4ª cobrança em 5 minutos, somando todos os
# lojistas. Com um número fixo, a demo só poderia rodar uma vez a cada 5 minutos, então cada execução gera o seu.
# O último dígito é o verificador de Luhn, a mesma conta que a API confere antes de aceitar o número.
random_card() {
	local digits=4 sum=0 position digit
	for ((position = 0; position < 14; position++)); do
		digits+=$((RANDOM % 10))
	done
	for ((position = 0; position < ${#digits}; position++)); do
		digit=${digits:${#digits}-1-position:1}
		if ((position % 2 == 0)); then
			digit=$((digit * 2))
			if ((digit > 9)); then
				digit=$((digit - 9))
			fi
		fi
		sum=$((sum + digit))
	done
	echo "$digits$(((10 - sum % 10) % 10))"
}

charge_body() {
	printf '{"amountCents":%d,"currency":"BRL","card":{"number":"%s","expiry":"2030-12","cvv":"123"}}' "$1" "$2"
}

new_merchant_body() {
	printf '{"name":"%s","email":"demo-%s@example.com"}' "$1" "$(uuidgen)"
}

if ! curl -sf -o /dev/null "$BASE_URL/actuator/health"; then
	echo "A API não respondeu em $BASE_URL/actuator/health. Confira se a URL termina em /api e se a aplicação está no ar." >&2
	exit 1
fi
echo "Demo do gateway de pagamento em $BASE_URL"

CARD=$(random_card)

step "1. Cadastro de lojista. A API key completa só aparece nesta resposta, e o banco guarda apenas o hash dela."
request POST /merchants "$(new_merchant_body "Loja Demo")"
expect_status 201
API_KEY=$(field apiKey)
AUTH=(-H "X-Api-Key: $API_KEY")
echo "   chave recebida: ${API_KEY:0:12}... (só esse prefixo fica em claro no banco)"

step "2. Sem a chave, as rotas de cobrança respondem 401."
request POST /charges "$(charge_body 15000 "$CARD")"
expect_status 401

step '3. Cobrança de R$ 150,00 com um cartão comum. O antifraude aprova.'
IDEMPOTENCY_KEY=$(uuidgen)
request POST /charges "$(charge_body 15000 "$CARD")" "${AUTH[@]}" -H "Idempotency-Key: $IDEMPOTENCY_KEY"
expect_status 201
CHARGE_ID=$(field id)
echo "   status=$(field status) id=$CHARGE_ID"

step "4. O mesmo pedido de novo, com a mesma Idempotency-Key. Volta a mesma cobrança, sem criar outra."
request POST /charges "$(charge_body 15000 "$CARD")" "${AUTH[@]}" -H "Idempotency-Key: $IDEMPOTENCY_KEY"
expect_status 201
if [[ $(field id) != "$CHARGE_ID" ]]; then
	echo "   Esperava o id $CHARGE_ID e veio $(field id)." >&2
	exit 1
fi
echo "   id=$(field id), o mesmo do passo 3"

step "5. A mesma chave com outro valor. A chave já pertence a outro pedido, então a resposta é 422."
request POST /charges "$(charge_body 99900 "$CARD")" "${AUTH[@]}" -H "Idempotency-Key: $IDEMPOTENCY_KEY"
expect_status 422
echo "   $(field detail)"

step "6. Cartão de teste com final 0002. A cobrança é criada, mas sai recusada."
request POST /charges "$(charge_body 15000 "$DECLINED_CARD")" "${AUTH[@]}" -H "Idempotency-Key: $(uuidgen)"
expect_status 201
echo "   status=$(field status) motivo=$(field declineReason)"

step "7. Captura da cobrança autorizada no passo 3."
request POST "/charges/$CHARGE_ID/capture" "" "${AUTH[@]}" -H "Idempotency-Key: $(uuidgen)"
expect_status 200
echo "   status=$(field status)"

step "8. Capturar de novo. A máquina de estados não deixa, e a resposta é 409."
request POST "/charges/$CHARGE_ID/capture" "" "${AUTH[@]}" -H "Idempotency-Key: $(uuidgen)"
expect_status 409
echo "   $(field detail)"

step "9. Estorno da cobrança capturada."
request POST "/charges/$CHARGE_ID/refund" "" "${AUTH[@]}" -H "Idempotency-Key: $(uuidgen)"
expect_status 200
echo "   status=$(field status)"

step "10. Consulta da cobrança, com o histórico completo de estados."
request GET "/charges/$CHARGE_ID" "" "${AUTH[@]}"
expect_status 200
echo "   histórico: $(state_history)"

step "11. Outro lojista tenta consultar a mesma cobrança e recebe 404, como se ela não existisse."
request POST /merchants "$(new_merchant_body "Outra Loja")"
expect_status 201
OTHER_API_KEY=$(field apiKey)
request GET "/charges/$CHARGE_ID" "" -H "X-Api-Key: $OTHER_API_KEY"
expect_status 404

printf '\nDemo concluída: todos os passos deram o resultado esperado.\n'
