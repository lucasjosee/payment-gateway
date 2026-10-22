# Roteiro de demo

Cinco minutos pela interface, com o console aberto o tempo todo. A ideia é mostrar o que o gateway garante, e não só as telas: cada clique vira uma linha no console, e o console liga o que aparece na tela aos logs do servidor.

## Antes de começar

- Suba o banco, a API com o profile `demo` e o front, como em [Como rodar](../README.md#interface-e-api).
- Abra `http://localhost:5173` numa janela anônima. Assim a sessão e as preferências do console começam limpas.
- Confira no rodapé o selo **API no ar**.
- Deixe à mão um terminal e um bloco de notas, para guardar a API key e rodar um `curl` no meio da demo.

## 0:00 a 0:30 · Abertura

Na tela de boas-vindas:

> O Ponte é um gateway de pagamento de estudo. Não tem banco de verdade por trás: quem aprova ou recusa é um antifraude da própria API. O que eu quero mostrar são as garantias que um gateway precisa ter: não cobrar duas vezes o mesmo pedido, não deixar duas operações ao mesmo tempo estragarem uma cobrança e não guardar o cartão. Embaixo da página fica um console, como o DevTools, com tudo o que a página pede à API.

## 0:30 a 1:00 · Criar o lojista

1. Em **Criar lojista de teste**, preencha nome e e-mail e clique em **Criar lojista**.
2. Mostre o aviso de que a chave aparece uma única vez, porque o banco guarda só o hash dela. Clique em **Copiar chave**, cole a chave no bloco de notas e clique em **Já guardei, continuar**.
3. No console, abra a linha `POST /api/merchants`. Na resposta, a `apiKey` aparece mascarada: nem o console da página fica com a chave inteira.

## 1:00 a 2:00 · Três cobranças, três resultados

1. Em **Nova cobrança**, clique em **Aprovado**, digite `150,00` no valor e clique em **Criar cobrança**. A cobrança nasce **Autorizada**.
2. Clique em **Recusado (final 0002)** e em **Criar cobrança**. O valor continua no campo. A cobrança nasce **Recusada**, com o motivo.
3. Faça o mesmo com **Moeda USD**: recusada porque o gateway só cobra em reais.
4. No console, abra o último `POST /api/charges` e aponte três coisas no envio: a `Idempotency-Key` que o front gerou, a API key e o cartão mascarados, e o valor em centavos em `amountCents`.

> Uma cobrança recusada também volta com 201. Ela foi criada, só que já nasceu recusada pelo antifraude.

## 2:00 a 3:00 · Máquina de estados

1. Em **Cobranças**, abra a cobrança aprovada.
2. Clique em **Capturar** e depois em **Estornar**. A linha do tempo ganha um evento a cada ação, e os botões que não valem no estado atual ficam desabilitados.
3. Ligue o **Modo demonstração** e clique em **Capturar** de novo. A API responde **409**: uma cobrança estornada não pode ser capturada.

   > O front desabilita o botão só para ajudar quem usa a tela. Quem decide é a API, porque qualquer um pode chamá-la direto, sem passar pelo front.

4. No console, abra a captura que deu certo (a de status 200) e clique em **Copiar como curl**. Cole no terminal, troque a chave mascarada (`gw_…•••`) pela chave do bloco de notas e rode. A resposta mostra a cobrança **Capturada**, mesmo ela já estando estornada.

   > É a idempotência: o pedido usa a mesma `Idempotency-Key`, então a API devolve a resposta guardada da primeira vez, sem capturar de novo.

## 3:00 a 4:30 · Laboratório de garantias

1. Em **Laboratório**, clique em **Enviar duas vezes**. O mesmo pedido de cobrança vai duas vezes com a mesma chave, e as duas respostas aparecem lado a lado, idênticas. Só uma cobrança foi criada.
2. Abra a aba **Servidor** do console. Perto do fim está a linha do segundo envio: *Idempotency-Key repetida com o mesmo pedido: devolvendo a resposta guardada, sem executar de novo*. Volte para **Requisições**.
3. Na concorrência, deixe 20 e clique em **Disparar capturas**. O placar mostra 1 sucesso e 19 conflitos, e as capturas ficam destacadas no console.
4. Aponte as mensagens de 409 no placar. Quase sempre aparecem duas: umas capturas foram barradas pelo lock otimista, quando dois pedidos tentaram gravar ao mesmo tempo, e as outras pela máquina de estados, porque chegaram depois de a cobrança já estar capturada.
5. Abra a aba **Servidor** de novo e mostre as linhas *Recusado com 409*. Clique no request ID de uma delas para ver só as linhas daquele pedido, com o SQL que ele executou.

## 4:30 a 5:00 · Fechamento

> O request ID amarra tudo. É o mesmo na linha do console, na mensagem de erro da tela e nos logs do servidor. Se um lojista reclama de um erro, esse ID leva direto às linhas daquele pedido.

Para terminar, mostre a [arquitetura alvo](../README.md#arquitetura-alvo) no README: o front no S3, a API numa EC2 e o banco num RDS, tudo atrás do CloudFront, no mesmo domínio.

## Se algo der errado

- **As telas dizem que a API parece estar fora do ar:** a API não subiu ou caiu. Depois de subir de novo, use **Tentar de novo** na tela. O selo do rodapé confere a API só quando a página carrega, então ele pode continuar dizendo que ela está no ar.
- **A aba Servidor diz que a API não tem o stream de logs:** a API subiu sem o profile `demo`.
- **Os logs de um pedido não aparecem na aba Servidor:** o servidor guarda só as últimas 200 linhas de cada lojista, e as 20 capturas da concorrência empurram as linhas mais antigas para fora. Por isso os logs da idempotência vêm antes da concorrência.
- **O `curl` copiado responde 401:** a chave mascarada não foi trocada pela chave inteira, ou a chave foi colada sem algum caractere.
