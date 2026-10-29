#!/usr/bin/env bash
# User data da EC2 (Amazon Linux 2023). O cloud-init roda este script como root uma única vez, no primeiro boot,
# e deixa a máquina pronta para o subir-api.sh. A saída vai para /var/log/cloud-init-output.log.
set -euo pipefail

dnf install -y docker git
systemctl enable --now docker

# A t4g.micro tem 1 GB de RAM, pouco para o Maven compilar a API dentro do Docker. Com o swap, o build fica mais
# lento, mas não é derrubado por falta de memória.
if [[ ! -f /swapfile ]]; then
	dd if=/dev/zero of=/swapfile bs=1M count=2048 status=none
	chmod 600 /swapfile
	mkswap /swapfile
	swapon /swapfile
	echo '/swapfile none swap defaults 0 0' >>/etc/fstab
fi

# O repositório é clonado aqui porque é nele que está o subir-api.sh, o próximo passo depois do boot.
git clone https://github.com/lucasjosee/payment-gateway.git /opt/payment-gateway
