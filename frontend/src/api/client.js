// Todas as chamadas à API passam por este arquivo, e nenhum componente usa o fetch direto. É isso que torna o console
// possível: o client avisa quem estiver inscrito a cada mudança de uma requisição, do início ao fim.

const BASE_PATH = '/api'
const MASK = '•••'

// Os mesmos 12 caracteres que o backend guarda em claro para encontrar a chave. O resto dela nunca vai para o console.
const VISIBLE_KEY_LENGTH = 12

let apiKey = null
const listeners = new Set()

export class ApiError extends Error {
  // status 0 quer dizer que a API nem respondeu. Nos outros casos, problem é o Problem Details que ela devolveu.
  constructor(status, problem, requestId) {
    super(problem?.detail ?? (status === 0 ? 'A API não respondeu.' : `A API respondeu com o status ${status}.`))
    this.name = 'ApiError'
    this.status = status
    this.problem = problem
    this.requestId = requestId
  }
}

// Fica só em memória. Quem decide onde a chave é guardada entre um carregamento e outro chama isto de novo.
export function setApiKey(key) {
  apiKey = key
}

export function maskApiKey(key) {
  return key.slice(0, VISIBLE_KEY_LENGTH) + MASK
}

// O listener recebe a requisição inteira a cada mudança. Devolve a função que cancela a inscrição.
export function subscribeToRequests(listener) {
  listeners.add(listener)
  return () => listeners.delete(listener)
}

export const api = {
  get: (path) => request('GET', path),
  // idempotent: true gera uma Idempotency-Key nova para o pedido. Quem precisa mostrar ou repetir a chave, como a
  // tela de nova cobrança, passa a sua em idempotencyKey. A tag marca os pedidos de um mesmo experimento do
  // laboratório, para o console destacá-los juntos.
  post: (path, { body, idempotent = false, idempotencyKey, tag } = {}) =>
    request('POST', path, {
      body,
      idempotencyKey: idempotencyKey ?? (idempotent ? crypto.randomUUID() : undefined),
      tag,
    }),
}

async function request(method, path, { body, idempotencyKey, tag } = {}) {
  const headers = {}
  if (apiKey) {
    headers['X-Api-Key'] = apiKey
  }
  if (idempotencyKey) {
    headers['Idempotency-Key'] = idempotencyKey
  }
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }

  const entry = {
    id: crypto.randomUUID(),
    tag: tag ?? null,
    method,
    path: BASE_PATH + path,
    startedAt: new Date().toISOString(),
    requestHeaders: maskHeaders(headers),
    requestBody: maskSecrets(body),
    status: null,
    durationMs: null,
    requestId: null,
    responseHeaders: null,
    responseBody: null,
    error: null,
  }
  publish(entry)

  const start = performance.now()
  let response
  let responseBody
  try {
    response = await fetch(BASE_PATH + path, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    })
    responseBody = await readBody(response)
  } catch {
    publish({ ...entry, durationMs: millisSince(start), error: 'A API não respondeu.' })
    throw new ApiError(0, null, null)
  }

  const requestId = response.headers.get('X-Request-Id')
  publish({
    ...entry,
    status: response.status,
    durationMs: millisSince(start),
    requestId,
    responseHeaders: Object.fromEntries(response.headers),
    responseBody: maskSecrets(responseBody),
  })

  if (!response.ok) {
    throw new ApiError(response.status, responseBody, requestId)
  }
  return responseBody
}

// Fica fora da lista de requisições do console, onde apareceria como um pedido que nunca termina.
export async function streamServerLogs({ signal, onOpen, onLine }) {
  const response = await fetch(`${BASE_PATH}/dev/logs`, {
    headers: { 'X-Api-Key': apiKey, Accept: 'text/event-stream' },
    signal,
  })
  if (!response.ok) {
    throw new ApiError(response.status, await readBody(response), response.headers.get('X-Request-Id'))
  }
  onOpen()
  await readEvents(response.body, (data) => onLine(JSON.parse(data)))
}

// Um pedaço lido da rede pode cortar um evento no meio. O que sobra no buffer espera o próximo pedaço.
async function readEvents(body, onData) {
  const reader = body.pipeThrough(new TextDecoderStream()).getReader()
  let buffer = ''
  while (true) {
    const { value, done } = await reader.read()
    if (done) {
      return
    }
    buffer = (buffer + value).replaceAll('\r\n', '\n')
    let end
    while ((end = buffer.indexOf('\n\n')) !== -1) {
      const data = buffer
        .slice(0, end)
        .split('\n')
        .filter((line) => line.startsWith('data:'))
        .map((line) => line.slice('data:'.length).replace(/^ /, ''))
        .join('\n')
      buffer = buffer.slice(end + 2)
      if (data) {
        onData(data)
      }
    }
  }
}

function publish(entry) {
  listeners.forEach((listener) => listener(entry))
}

// Um corpo que diz ser JSON mas não é aparece como texto no console, em vez de derrubar a requisição inteira.
async function readBody(response) {
  const text = await response.text()
  if (!text) {
    return null
  }
  try {
    return JSON.parse(text)
  } catch {
    return text
  }
}

function maskHeaders(headers) {
  if (!headers['X-Api-Key']) {
    return headers
  }
  return { ...headers, 'X-Api-Key': maskApiKey(headers['X-Api-Key']) }
}

// O console mostra os corpos, mas nunca um segredo inteiro. Do cartão fica só o final, como a própria API guarda.
// A API key aparece por inteiro numa única resposta, a do cadastro do lojista, e também é mascarada.
// A máscara vale só para a cópia do console: quem chamou recebe os dados reais.
function maskSecrets(body) {
  if (body === null || typeof body !== 'object') {
    return body ?? null
  }
  const masked = { ...body }
  if (body.card) {
    masked.card = { ...body.card, number: `•••• ${String(body.card.number).slice(-4)}`, cvv: MASK }
  }
  if (typeof body.apiKey === 'string') {
    masked.apiKey = maskApiKey(body.apiKey)
  }
  return masked
}

function millisSince(start) {
  return Math.round(performance.now() - start)
}
