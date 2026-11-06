// O navegador só oferece o crypto.randomUUID em HTTPS ou no localhost. Pelo IP da máquina na rede, como no teste do
// QR code com o celular, ele não existe, e sem ele nenhuma chamada à API sai.
if (!crypto.randomUUID) {
  crypto.randomUUID = () => {
    const bytes = crypto.getRandomValues(new Uint8Array(16))
    // Estes dois bytes guardam a versão e a variante, que é o que faz os bytes aleatórios serem um UUID v4.
    bytes[6] = (bytes[6] & 0x0f) | 0x40
    bytes[8] = (bytes[8] & 0x3f) | 0x80
    const hex = Array.from(bytes, (byte) => byte.toString(16).padStart(2, '0')).join('')
    return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`
  }
}
