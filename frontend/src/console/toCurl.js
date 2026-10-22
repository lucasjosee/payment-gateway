// Monta um curl que repete a requisição de uma entrada do console. Ele usa os headers e o corpo do jeito que o
// console guardou, então a API key e o cartão já vêm mascarados pelo client. Para rodar no terminal, cole os valores
// reais no lugar das máscaras.
//
// O origin é o da própria página, e não o do backend: em desenvolvimento o pedido passa pelo proxy do Vite, e na AWS
// pelo CloudFront, exatamente como o navegador fez.
export function toCurl(entry, origin) {
  const command = entry.method === 'GET' ? 'curl' : `curl -X ${entry.method}`
  const parts = [`${command} ${quote(origin + entry.path)}`]

  for (const [name, value] of Object.entries(entry.requestHeaders)) {
    parts.push(`-H ${quote(`${name}: ${value}`)}`)
  }
  if (entry.requestBody !== null) {
    parts.push(`--data-raw ${quote(JSON.stringify(entry.requestBody))}`)
  }

  return parts.join(' \\\n  ')
}

// Entre aspas simples o shell não interpreta nada, nem $ nem crase. A única coisa a tratar é a própria aspa simples:
// ela fecha o trecho, entra escapada e abre um trecho novo.
function quote(value) {
  return `'${value.replaceAll("'", "'\\''")}'`
}
