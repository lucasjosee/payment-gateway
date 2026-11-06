# Roteiro de demo

Cerca de sete minutos pela interface, com o console aberto o tempo todo. A ideia é mostrar o que o gateway garante, e não só as telas: cada clique vira uma linha no console, e o console liga o que aparece na tela aos logs do servidor.

## Antes de começar

- Rode `./infra/ligar.sh`, que só termina quando a API responde pelo CloudFront, e abra o [site](https://d7wo0l0c3utlh.cloudfront.net) numa janela anônima. Assim a sessão e as preferências do console começam limpas.
- Confira no rodapé o selo **API no ar**.
- Deixe à mão um terminal e um bloco de notas, para guardar a API key e rodar um `curl` no meio da demo, e o celular com internet e a câmera pronta.

Para apresentar sem a AWS, suba tudo como em [Como rodar](../README.md#como-rodar), mas com `npm run dev -- --host`, e abra o front pelo IP da máquina, com o celular na mesma rede. Pelo IP o navegador não considera a página segura, e os botões de copiar mostram *Não deu para copiar*. Nesse caso, selecione a chave com o mouse e pule o `curl`.

## 0:00 a 0:30 · Abertura

Na tela de boas-vindas:

> O Ponte é um gateway de pagamento de estudo, com cartão e Pix, sem banco de verdade por trás. Quero mostrar o que um gateway precisa garantir: não cobrar duas vezes o mesmo pedido, não deixar duas operações ao mesmo tempo estragarem uma cobrança, não guardar o cartão e não perder o aviso para o lojista. Embaixo da página, um console mostra tudo o que ela pede à API.

## 0:30 a 1:00 · Criar o lojista

1. Em **Criar lojista de teste**, preencha nome e e-mail e clique em **Criar lojista**.
2. Mostre o aviso de que a chave aparece uma única vez, porque o banco guarda só o hash dela. Clique em **Copiar chave**, cole a chave no bloco de notas e clique em **Já guardei, continuar**.
3. No console, abra a linha `POST /api/merchants`. Na resposta, a `apiKey` aparece mascarada: nem o console da página fica com a chave inteira.

## 1:00 a 1:30 · Cadastrar o webhook

1. Em **Webhooks**, clique em **Usar o receptor de teste** e em **Salvar**.

   > O receptor de teste faz o papel do servidor da loja, dentro da própria API. Uma URL de verdade precisa usar https e apontar para um endereço público. É a proteção contra SSRF: a API vai chamar um endereço que o lojista escolheu, e ele não pode ser a rede interna da AWS.

2. Mostre o **Segredo de assinatura**, que aparece escondido. É com ele que a loja confere se um webhook veio mesmo do gateway.

## 1:30 a 2:15 · Três cobranças, três resultados

1. Em **Nova cobrança**, clique em **Aprovado**, digite `150,00` no valor e clique em **Criar cobrança**. A cobrança nasce **Autorizada**.
2. Clique em **Recusado (final 0002)** e em **Criar cobrança**. O valor continua no campo. A cobrança nasce **Recusada**, com o motivo.
3. Faça o mesmo com **Moeda USD**: recusada porque o gateway só cobra em reais.
4. No console, abra o último `POST /api/charges` e aponte três coisas no envio: a `Idempotency-Key` que o front gerou, a API key e o cartão mascarados, e o valor em centavos em `amountCents`.

> Uma cobrança recusada também volta com 201. Ela foi criada, só que já nasceu recusada pelo antifraude.

## 2:15 a 3:15 · Máquina de estados

1. Em **Cobranças**, abra a cobrança **Autorizada**.
2. Clique em **Capturar** e depois em **Estornar**. A linha do tempo ganha um evento a cada ação, e os botões que não valem no estado atual ficam desabilitados.
3. Ligue o **Modo demonstração** e clique em **Capturar** de novo. A API responde **409**: uma cobrança estornada não pode ser capturada.

   > O front desabilita o botão só para ajudar quem usa a tela. Quem decide é a API, porque qualquer um pode chamá-la direto, sem passar pelo front.

4. No console, abra a captura que deu certo (a de status 200) e clique em **Copiar como curl**. Cole no terminal, troque a chave mascarada (`gw_…•••`) pela chave do bloco de notas e rode. A resposta mostra a cobrança **Capturada**, mesmo ela já estando estornada.

   > É a idempotência: o pedido usa a mesma `Idempotency-Key`, então a API devolve a resposta guardada da primeira vez, sem capturar de novo.

## 3:15 a 4:30 · Pix pago pelo celular

1. Em **Nova cobrança**, clique em **Pix**, digite `42,50` no valor e clique em **Criar cobrança**. Os campos do cartão somem, e a cobrança nasce **Pendente**, com a contagem regressiva e o QR code.

   > No cartão, o gateway responde na hora. No Pix, quem toma a iniciativa é quem paga, então a cobrança fica esperando e pode vencer.

2. Aponte a câmera do celular para o QR code e abra o link. Mostre a tela: o valor, o nome da loja e o aviso de simulação, sem pedir API key.

   > O celular faz o papel do app do banco de quem paga. Este QR code leva um link porque não há banco por trás. Um Pix de verdade carrega um código no padrão BR Code.

3. Toque em **Pagar R$ 42,50**. O celular mostra **Pago**.
4. No computador, clique em **Ver detalhe da cobrança**. A cobrança está **Paga**, e a linha do tempo mostra *Pendente → Paga*. O clique é necessário porque a tela não se atualiza sozinha.
5. Em **Webhooks**, role até o **Receptor de teste** e aponte `charge.pending` e `charge.paid`, com **Assinatura válida**.

   > O pagamento passa pelo mesmo caminho de qualquer mudança de status. Se o prazo já tivesse vencido, a API responderia 409, mesmo que o job de expiração ainda não tivesse passado, porque quem confere o prazo é a própria cobrança.

## 4:30 a 5:45 · Laboratório: idempotência e concorrência

1. Em **Laboratório**, clique em **Enviar duas vezes**. O mesmo pedido de cobrança vai duas vezes com a mesma chave, e as duas respostas aparecem lado a lado, idênticas. Só uma cobrança foi criada.
2. Abra a aba **Servidor** do console. Perto do fim está a linha do segundo envio: *Idempotency-Key repetida com o mesmo pedido: devolvendo a resposta guardada, sem executar de novo*. Volte para **Requisições**.
3. Na concorrência, deixe 20 e clique em **Disparar capturas**. O placar mostra 1 sucesso, 19 conflitos e 1 webhook `charge.captured`, e as capturas ficam destacadas no console.
4. Aponte as mensagens de 409 no placar. Quase sempre aparecem duas: umas capturas foram barradas pelo lock otimista, quando dois pedidos tentaram gravar ao mesmo tempo, e as outras pela máquina de estados, porque chegaram depois de a cobrança já estar capturada.
5. Abra a aba **Servidor** de novo e mostre as linhas *Recusado com 409*. Clique no request ID de uma delas para ver só as linhas daquele pedido, com o SQL que ele executou. Clique em **Mostrar todos** e volte para **Requisições**.

## 5:45 a 6:45 · Laboratório: o webhook que não se perde

1. Role até **Entrega garantida** e clique em **Começar** e em **Tirar do ar**. O receptor passa a responder 503, como um servidor de loja que caiu.
2. Clique em **Criar cobrança**. Ela é criada normalmente, com o servidor da loja fora do ar.

   > A cobrança não depende do servidor da loja. O webhook é gravado na mesma transação dela, e um despachante envia depois. Se a API caísse agora, a entrega continuaria guardada no banco.

3. Em cerca de 6 segundos, a tabela mostra 2 tentativas, o status **503** e a **Próxima tentativa** marcada. Clique em **Religar**.
4. A próxima tentativa leva cerca de 15 segundos, e ninguém precisa pedir o reenvio. Use esse tempo para falar:

   > As esperas crescem para não insistir num servidor que acabou de cair. Aqui elas são de segundos, para dar tempo de ver. Fora da demonstração, vão de minutos a horas. O lojista pode receber o mesmo evento duas vezes, e é pelo `Ponte-Event-Id` que ele reconhece o que já processou.

5. O experimento mostra **Como esperado**: o evento chegou uma vez, com a assinatura válida, na tentativa 3.

## 6:45 a 7:15 · Fechamento

> O request ID amarra tudo. É o mesmo na linha do console, na mensagem de erro da tela e nos logs do servidor. Se um lojista reclama de um erro, esse ID leva direto às linhas daquele pedido.

Para terminar, mostre a [arquitetura na AWS](../README.md#arquitetura-na-aws) no README: o front no S3, a API numa EC2 e o banco num RDS, tudo atrás do CloudFront, no mesmo domínio, e os webhooks saindo da EC2 para o servidor da loja.

## Se o tempo apertar

O Pix e a entrega garantida são a novidade desta versão, então corte primeiro o que já vinha da anterior:

- O `curl` da máquina de estados economiza cerca de 30 segundos.
- A Moeda USD e o filtro por request ID economizam cerca de 20 segundos juntos.

## Se algo der errado

- **As telas dizem que a API parece estar fora do ar:** a API não subiu ou caiu. Depois de subir de novo, use **Tentar de novo** na tela. O selo do rodapé confere a API só quando a página carrega, então ele pode continuar dizendo que ela está no ar.
- **A aba Servidor diz que a API não tem o stream de logs:** a API subiu sem o profile `demo`.
- **Os logs de um pedido não aparecem na aba Servidor:** o servidor guarda só as últimas 200 linhas de cada lojista, e as 20 capturas da concorrência empurram as linhas mais antigas para fora. Por isso os logs da idempotência vêm antes da concorrência.
- **O `curl` copiado responde 401:** a chave mascarada não foi trocada pela chave inteira, ou a chave foi colada sem algum caractere.
- **O celular não abre o QR code:** use **Copiar link** e abra o endereço numa janela anônima do computador. O pagamento funciona igual.
- **O detalhe do Pix continua Pendente depois de pagar:** a tela não se atualiza sozinha. Clique em **Cobranças** e abra a cobrança de novo.
- **O Pix aparece como Vencida:** passaram os 15 minutos do prazo. Crie outro.
- **A Entrega garantida pede o receptor de teste:** o webhook aponta para outra URL ou não foi cadastrado. Faça o cadastro em **Webhooks** e clique em **Recomeçar**.
- **Nenhum webhook chega ao receptor:** o interruptor **Fora do ar** pode ter ficado ligado por um experimento abandonado. Desligue-o em **Webhooks**. O receptor guarda tudo só em memória, então uma API reiniciada volta com ele vazio e ligado.
