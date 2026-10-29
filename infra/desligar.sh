#!/usr/bin/env bash
# Para a EC2 e o RDS para não gastar enquanto ninguém usa o site. Nada é apagado: o banco e o disco da EC2
# continuam lá, só param de rodar. O ligar.sh volta tudo ao ar.
#
# Uso, no seu computador, com a AWS CLI logada (aws sso login): ./infra/desligar.sh
set -euo pipefail

INSTANCE_ID=i-0b7f2a08edb33d91a
DB_INSTANCE=gateway-db

# A API para primeiro para não ficar respondendo com erro enquanto o banco desliga.
echo "Parando a EC2..."
aws ec2 stop-instances --instance-ids "$INSTANCE_ID" >/dev/null
aws ec2 wait instance-stopped --instance-ids "$INSTANCE_ID"

# O RDS recusa o pedido de parar se não estiver available, por exemplo quando já está parado.
DB_STATUS=$(aws rds describe-db-instances \
	--db-instance-identifier "$DB_INSTANCE" \
	--query 'DBInstances[0].DBInstanceStatus' \
	--output text)
if [[ $DB_STATUS == available ]]; then
	echo "Parando o RDS..."
	aws rds stop-db-instance --db-instance-identifier "$DB_INSTANCE" >/dev/null
else
	echo "O RDS está com status $DB_STATUS, então não foi preciso pedir para parar."
fi

# A CLI não tem um wait para banco parado, e não faz diferença esperar: o pedido já foi aceito.
echo "Tudo desligado. O RDS ainda leva alguns minutos para chegar em stopped."
echo "A AWS religa sozinha um RDS parado há 7 dias. Se for ficar mais tempo sem usar, rode este script de novo."
