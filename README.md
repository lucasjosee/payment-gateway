# Ponte

Gateway de pagamento simplificado, feito como projeto de portfólio: uma API REST e uma interface web que mostra, embaixo de cada tela, as requisições feitas à API e os logs do servidor ao vivo. Um lojista se cadastra, recebe uma API key e com ela cria cobranças de cartão, que passam por um antifraude e depois podem ser capturadas, canceladas ou estornadas.

Não há banco emissor nem adquirente de verdade: a aprovação ou a recusa vem das regras de antifraude da própria API. O foco está no que um gateway real precisa garantir: não cobrar duas vezes o mesmo pedido, não deixar duas operações simultâneas corromperem uma cobrança e não guardar dados sensíveis do cartão.

**Backend:** Java 21, Spring Boot 4, Spring Security, Spring Data JPA, PostgreSQL 17, Flyway, springdoc-openapi, Testcontainers e Docker.

**Front-end:** React 19 com Vite, em JavaScript, React Router e CSS Modules. Sem biblioteca de componentes: os componentes base e os tokens de cor, espaçamento e tipografia são do próprio projeto.

**Infra:** AWS (CloudFront, S3, EC2, RDS e IAM Identity Center), com scripts em Bash e a AWS CLI.

## O que dá para fazer

- Criar um lojista de teste e receber a API key, que aparece uma única vez, ou entrar com a chave de um lojista já criado.
- Criar cobranças de cartão, com cartões de teste que levam à aprovação ou à recusa pelo antifraude.
- Listar as cobranças e abrir o detalhe de cada uma, com a linha do tempo dos estados.
- Capturar, cancelar ou estornar uma cobrança. O modo demonstração libera as ações inválidas para mostrar a API respondendo `409`.
- No Laboratório, repetir um pedido para ver a idempotência e disparar capturas simultâneas para ver só uma passar.
- No console, embaixo de cada tela, acompanhar as requisições à API e os logs do servidor ao vivo, e copiar qualquer requisição como `curl`.

## Arquitetura na AWS

```mermaid
flowchart LR
    browser([Navegador]) -->|HTTPS| cloudfront[CloudFront]
    cloudfront -->|"/*"| s3[("S3<br/>build do React, privado")]
    cloudfront -->|"/api/*"| ec2["EC2 :8080<br/>Spring Boot em Docker"]
    ec2 --> rds[("RDS<br/>PostgreSQL 17")]
```

- Front e API ficam no mesmo domínio, o do CloudFront. Por isso não há CORS nem mistura de HTTP com HTTPS, e todas as rotas da API ficam sob `/api` para o CloudFront saber para onde mandar cada pedido.
- Em desenvolvimento, o Vite faz o mesmo papel: serve o front e repassa o que começa com `/api` para `localhost:8080`.
- O bucket é privado: só o CloudFront lê os arquivos, por Origin Access Control. As rotas do React, como `/cobrancas/123`, não existem como arquivo, e uma CloudFront Function as troca pelo `index.html`. Páginas de erro personalizadas fariam o mesmo, mas valeriam também para a API e transformariam os `404` dela em HTML.
- O caminho `/api/*` não tem cache e repassa todos os headers do navegador, como `X-Api-Key` e `Idempotency-Key`.
- A EC2 só aceita tráfego vindo do CloudFront, pela prefix list que a AWS mantém com os IPs dele, e o RDS só aceita conexões vindas da EC2. Não há porta 22: o acesso à máquina é pelo Session Manager, e ela usa uma role IAM em vez de chaves de acesso.
- Fora das demos, a EC2 e o RDS ficam desligados para não gastar.

## Endpoints

| Método | Rota | Autenticação | `Idempotency-Key` | O que faz |
|---|---|---|---|---|
| `POST` | `/api/merchants` | não | não | Cadastra o lojista e devolve a API key, uma única vez |
| `GET` | `/api/merchants/me` | sim | não | Dados do lojista autenticado |
| `POST` | `/api/charges` | sim | obrigatória | Cria a cobrança, que já nasce `AUTHORIZED` ou `DECLINED` |
| `GET` | `/api/charges` | sim | não | Lista as cobranças do lojista, das mais recentes para as mais antigas, em páginas (`page` a partir de 0 e `size` de 1 a 100) |
| `GET` | `/api/charges/{id}` | sim | não | Consulta a cobrança com o histórico de estados |
| `POST` | `/api/charges/{id}/capture` | sim | obrigatória | Captura uma cobrança autorizada |
| `POST` | `/api/charges/{id}/cancel` | sim | obrigatória | Cancela uma cobrança autorizada |
| `POST` | `/api/charges/{id}/refund` | sim | obrigatória | Estorna uma cobrança capturada |
| `GET` | `/api/dev/logs` | sim | não | Logs dos pedidos do próprio lojista, ao vivo por Server-Sent Events. Só existe com o profile `demo` |

A autenticação é pelo header `X-Api-Key`. A documentação interativa fica em `/api/swagger-ui.html`, com o botão *Authorize* para informar a chave.

Toda resposta traz o header `X-Request-Id`, com o mesmo valor que aparece nos logs daquele pedido.

## Diagramas

O caminho de um pedido de cobrança pela aplicação:

```mermaid
flowchart LR
    client([Lojista]) -->|X-Api-Key| filter[Filtro de API key]
    filter --> controller[ChargeController]
    controller --> idempotency[IdempotencyService]
    idempotency --> service[ChargeService]
    service --> fraud[FraudService]
    service --> charge[Charge]
    idempotency --> db[(PostgreSQL)]
    service --> db
```

Os estados de uma cobrança. Qualquer transição fora destas responde `409`:

```mermaid
stateDiagram-v2
    [*] --> AUTHORIZED: antifraude aprova
    [*] --> DECLINED: antifraude recusa
    AUTHORIZED --> CAPTURED: capture
    AUTHORIZED --> CANCELED: cancel
    CAPTURED --> REFUNDED: refund
```

## Como rodar

### Interface e API

Precisa do Docker, do Java 21 e do Node.js 22 (ou 20.19 em diante). São três terminais: o banco, a API e o front.

```bash
# 1. Banco
docker compose up -d db

# 2. API, com o profile demo para a aba Servidor do console funcionar
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo

# 3. Front
cd frontend
npm install
npm run dev
```

A interface abre em `http://localhost:5173`. Para conferir o build de produção, use `npm run build` e depois `npm run preview`, que abre em `http://localhost:4173` com o mesmo repasse de `/api`.

Para apresentar o projeto pela interface, há um [roteiro de demo de 5 minutos](docs/roteiro-demo.md).

### Só a API, com Docker

Um comando sobe o banco e a API:

```bash
docker compose up --build
```

A API fica em `http://localhost:8080/api` e o Swagger em `http://localhost:8080/api/swagger-ui.html`. O front também funciona com ela, mas sem o profile `demo` a aba Servidor do console fica indisponível.

### Demo pelo terminal

Com a API no ar, o script abaixo percorre o fluxo principal com `curl`: cadastro, cobrança aprovada, repetição idempotente, cobrança recusada, captura, estorno e isolamento entre lojistas. Ele confere a resposta de cada passo e para no primeiro que não sair como esperado.

```bash
./scripts/demo.sh http://localhost:8080/api
```

### Logs ao vivo (profile `demo`)

Com o profile `demo`, a API transmite ao vivo os logs e o SQL dos pedidos de cada lojista. A aba Servidor do console mostra esse stream, e pelo terminal também dá para acompanhar:

```bash
curl -N http://localhost:8080/api/dev/logs -H "X-Api-Key: <chave>"
```

### Testes e verificações

Os testes do backend rodam com `./mvnw test`, de dentro de `backend/`, e precisam do Docker rodando, com o banco do `docker compose` de pé. No front, `npm run lint` roda o ESLint.

### Variáveis de ambiente

| Variável | Para que serve |
|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Conexão com o PostgreSQL |
| `CARD_FINGERPRINT_KEY` | Chave do HMAC que identifica um cartão sem guardar o número |
| `IDEMPOTENCY_HASH_KEY` | Chave do HMAC que identifica o conteúdo de um pedido idempotente |

Todas têm valores padrão que só servem para rodar localmente. Em qualquer outro ambiente, precisam ser definidas.

### Deploy na AWS

Os recursos foram criados pelo console, e o dia a dia fica com os scripts de `infra/`, que guardam os IDs da conta em variáveis no início de cada arquivo. Os que rodam localmente usam a AWS CLI logada pelo IAM Identity Center (`aws sso login`).

| Arquivo | Onde roda | O que faz |
|---|---|---|
| `user-data.sh` | EC2, no primeiro boot | Instala Docker e Git, cria swap para o build caber na memória e clona o repositório |
| `subir-api.sh` | EC2, pelo Session Manager | Atualiza o código, gera a imagem e troca o container da API. Pede a senha do banco na hora, para ela não ficar em arquivo |
| `deploy-front.sh` | Local | Faz o build do front, envia para o S3 e invalida o cache do CloudFront |
| `ligar.sh` | Local | Liga o RDS, depois a EC2, e espera a API responder pelo CloudFront |
| `desligar.sh` | Local | Para a EC2 e o RDS |
| `spa-routes.js` | CloudFront | Function que manda as rotas do React para o `index.html` |

## Decisões

- **Dinheiro em centavos.** Valores são `long` no Java e `bigint` no banco. Com `double`, R$ 0,10 + R$ 0,20 não daria exatamente R$ 0,30.
- **API key guardada só como hash.** A chave completa aparece uma única vez, na resposta do cadastro. O banco guarda o prefixo, usado para encontrar a chave, e o SHA-256 dela, comparado em tempo constante. Se o banco vazar, as chaves continuam seguras.
- **Nada do cartão que permita reconstruí-lo.** Ficam só os 4 últimos dígitos e um *fingerprint* HMAC-SHA256, com uma chave secreta da aplicação, que deixa o antifraude reconhecer o mesmo cartão sem saber o número. O CVV é validado e descartado.
- **Regras de estado dentro da entidade.** A `Charge` decide sozinha se uma transição é válida e registra o evento no histórico. Os controllers e services não mexem no status diretamente.
- **Idempotência no banco.** Cada `Idempotency-Key` é registrada por lojista com um único `INSERT ... ON CONFLICT`, então dois pedidos simultâneos com a mesma chave nunca são executados os dois. O pedido é identificado por um HMAC de método, rota e corpo. Uma repetição igual recebe a resposta guardada, uma repetição com conteúdo diferente recebe `422`, e uma chave ainda em andamento recebe `409`. As chaves valem por 24 horas.
- **Lock otimista contra operações simultâneas.** A `Charge` tem `@Version`, e o Hibernate só grava se a versão não mudou desde a leitura. De duas capturas simultâneas da mesma cobrança, uma passa e a outra recebe `409`.
- **Open Session In View desligado.** Ligado, que é o padrão do Spring Boot, cada requisição segura a conexão com o banco até o fim, e com muitas falhas simultâneas o pool de conexões trava. Tudo o que vem do banco é convertido em DTO ainda dentro da transação.
- **`404` para a cobrança de outro lojista.** Um `403` confirmaria que o id existe. Com `404`, ela é indistinguível de uma cobrança inexistente.
- **Antifraude em regras independentes.** Cada regra implementa `FraudRule` e o `FraudService` aplica todas em ordem, parando na primeira recusa. São quatro: moeda diferente de BRL, cartão de teste com final `0002`, valor acima de R$ 10.000,00 e, a partir da quarta, cobranças do mesmo cartão em menos de cinco minutos.
- **Erros no formato Problem Details (RFC 9457).** Todo erro tem a mesma estrutura, inclusive os de validação, que listam os campos inválidos.
- **Schema versionado com Flyway.** As migrations criam e alteram as tabelas. O Hibernate só valida se as entidades batem com o banco e impede a aplicação de subir se não baterem.

## Próximos passos

- **Origem mais fechada na AWS.** O trecho entre o CloudFront e a EC2 é HTTP, e a prefix list aceita qualquer distribuição do CloudFront, não só a deste projeto. HTTPS nesse trecho e um header secreto conferido pela API fechariam os dois pontos.
- **Limpeza periódica** das chaves de idempotência expiradas, que hoje só são reaproveitadas quando a mesma chave volta.
- **Webhooks** para avisar o lojista quando uma cobrança muda de estado.
- **Captura e estorno parciais**, que hoje sempre usam o valor inteiro da cobrança.
- **Limite de requisições** por API key.
