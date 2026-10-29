// CloudFront Function (runtime cloudfront-js-2.0) do behavior padrão, o que serve o S3. As rotas do React, como
// /cobrancas/123, não existem como arquivo no bucket, então viram /index.html e o React Router escolhe a tela no
// navegador. Os arquivos do build, como /assets/index-abc123.js, têm extensão e passam sem mudança.
// A API fica no behavior /api/* e não passa por aqui, por isso os 404 dela continuam chegando como 404.
function handler(event) {
  const request = event.request
  const lastSegment = request.uri.split('/').pop()
  if (!lastSegment.includes('.')) {
    request.uri = '/index.html'
  }
  return request
}
