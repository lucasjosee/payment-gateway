#!/usr/bin/env bash
# Atualiza o código, gera a imagem da API e troca o container que está rodando. Serve tanto para o primeiro deploy
# quanto para publicar uma versão nova: é só rodar de novo.
#
# Uso, dentro da EC2 pelo Session Manager: sudo /opt/payment-gateway/infra/subir-api.sh
set -euo pipefail

DB_ENDPOINT=gateway-db.cb2y6qcm0cak.sa-east-1.rds.amazonaws.com
DB_USERNAME=gateway
CONTAINER=gateway-api
HEALTH_URL=http://localhost:8080/api/actuator/health

# O usuário do Session Manager não está no grupo do Docker, e o clone em /opt pertence ao root.
if [[ $EUID -ne 0 ]]; then
	echo "Rode com sudo: sudo $0" >&2
	exit 1
fi

REPO_DIR=$(cd "$(dirname "$0")/.." && pwd)
git -C "$REPO_DIR" pull --ff-only

# A senha é pedida a cada execução para nunca ficar no repositório nem no histórico do shell.
read -rsp "Senha do banco ($DB_USERNAME): " DB_PASSWORD
echo
export DB_PASSWORD

docker build -t "$CONTAINER" "$REPO_DIR/backend"

docker rm -f "$CONTAINER" >/dev/null 2>&1 || true
# O -e DB_PASSWORD sem valor copia a variável do ambiente, assim a senha não aparece na linha de comando do processo.
# O Docker guarda as variáveis junto com o container, e é isso que permite o --restart subir a API sozinha
# quando a EC2 for religada.
docker run -d \
	--name "$CONTAINER" \
	--restart unless-stopped \
	-p 8080:8080 \
	-e DB_URL="jdbc:postgresql://$DB_ENDPOINT:5432/gateway" \
	-e DB_USERNAME="$DB_USERNAME" \
	-e DB_PASSWORD \
	-e SPRING_PROFILES_ACTIVE=demo \
	"$CONTAINER"

# Cada build deixa a imagem anterior sem nome, e o disco da EC2 é pequeno.
docker image prune -f >/dev/null

echo "Esperando a API responder..."
for _ in $(seq 1 90); do
	if curl -sf -o /dev/null "$HEALTH_URL"; then
		echo "API no ar."
		exit 0
	fi
	sleep 2
done

echo "A API não respondeu em 3 minutos. Últimas linhas do log:" >&2
docker logs --tail 50 "$CONTAINER" >&2
exit 1
