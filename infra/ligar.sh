#!/usr/bin/env bash
# Religa o RDS e a EC2, nessa ordem, e só termina quando a API responde pelo CloudFront.
#
# Uso, no seu computador, com a AWS CLI logada (aws sso login): ./infra/ligar.sh
set -euo pipefail

INSTANCE_ID=i-0b7f2a08edb33d91a
DB_INSTANCE=gateway-db
SITE_URL=https://d7wo0l0c3utlh.cloudfront.net

DB_STATUS=$(aws rds describe-db-instances \
	--db-instance-identifier "$DB_INSTANCE" \
	--query 'DBInstances[0].DBInstanceStatus' \
	--output text)
case $DB_STATUS in
	stopped)
		echo "Ligando o RDS..."
		aws rds start-db-instance --db-instance-identifier "$DB_INSTANCE" >/dev/null
		;;
	stopping)
		echo "O RDS ainda está terminando de desligar. Espere alguns minutos e rode de novo." >&2
		exit 1
		;;
esac

# O banco precisa estar pronto antes da API: o Spring Boot roda as migrations do Flyway ao iniciar e desiste se
# não conseguir conectar. O Docker tentaria de novo, mas com esperas cada vez maiores.
echo "Esperando o RDS ficar disponível. Costuma levar de 5 a 10 minutos..."
aws rds wait db-instance-available --db-instance-identifier "$DB_INSTANCE"

echo "Ligando a EC2..."
aws ec2 start-instances --instance-ids "$INSTANCE_ID" >/dev/null
aws ec2 wait instance-running --instance-ids "$INSTANCE_ID"

# Não é preciso entrar na máquina: o Docker inicia com ela e sobe o container sozinho, por causa do
# --restart unless-stopped do subir-api.sh. Falta só o Spring Boot terminar de iniciar.
echo "Esperando a API responder pelo CloudFront..."
for _ in $(seq 1 60); do
	if curl -sf -o /dev/null "$SITE_URL/api/actuator/health"; then
		echo "Site no ar: $SITE_URL"
		exit 0
	fi
	sleep 5
done

echo "A API não respondeu em 5 minutos. Entre na EC2 pelo Session Manager e veja o log com: sudo docker logs gateway-api" >&2
exit 1
