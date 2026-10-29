#!/usr/bin/env bash
# Gera o build do front, publica no bucket e invalida o cache do CloudFront. Termina só quando a versão nova
# já está sendo servida.
#
# Uso, no seu computador, com a AWS CLI logada (aws sso login): ./infra/deploy-front.sh
set -euo pipefail

BUCKET=gateway-front-lucasjosee
DISTRIBUTION_ID=E3VR4C2QCDY7O0
SITE_URL=https://d7wo0l0c3utlh.cloudfront.net

REPO_DIR=$(cd "$(dirname "$0")/.." && pwd)

npm --prefix "$REPO_DIR/frontend" run build

# O --delete apaga do bucket o que não existe mais no build, como os arquivos com o hash da versão anterior.
aws s3 sync "$REPO_DIR/frontend/dist" "s3://$BUCKET" --delete

# As edge locations guardam o index.html por até um dia. Sem invalidar, elas seguiriam entregando a versão antiga.
INVALIDATION_ID=$(aws cloudfront create-invalidation \
	--distribution-id "$DISTRIBUTION_ID" \
	--paths '/*' \
	--query Invalidation.Id \
	--output text)
echo "Esperando a invalidação $INVALIDATION_ID terminar..."
aws cloudfront wait invalidation-completed --distribution-id "$DISTRIBUTION_ID" --id "$INVALIDATION_ID"

echo "Front publicado em $SITE_URL"
