package com.lucas.gateway.webhook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

// Decide se uma URL pode receber webhooks. A decisão olha os endereços para onde o host resolve, e não o texto da
// URL, porque qualquer nome pode apontar para 127.0.0.1, e o mesmo endereço pode ser escrito de vários jeitos, como
// 2130706433 ou [::ffff:127.0.0.1].
@Component
public class WebhookUrlValidator {

	interface HostResolver {
		InetAddress[] resolve(String host) throws UnknownHostException;
	}

	// Faixas que não levam à internet pública: as redes privadas (10, 172.16 e 192.168), o loopback (127), o
	// link-local (169.254, onde fica o 169.254.169.254, o serviço de metadados da EC2), a faixa que os provedores usam
	// para NAT (100.64), o multicast (224) e as reservadas. O 0.0.0.0/8 entra porque o Linux trata o 0.0.0.0 como a
	// própria máquina.
	private static final List<Ipv4Range> NON_PUBLIC_IPV4 = Stream.of(
			"0.0.0.0/8", "10.0.0.0/8", "100.64.0.0/10", "127.0.0.0/8", "169.254.0.0/16", "172.16.0.0/12",
			"192.0.0.0/24", "192.168.0.0/16", "198.18.0.0/15", "224.0.0.0/4", "240.0.0.0/4")
			.map(Ipv4Range::parse)
			.toList();

	private final HostResolver resolver;
	// Fora do profile demo a propriedade não existe, o valor fica vazio e nenhuma URL escapa da validação.
	private final String testReceiverBaseUrl;

	@Autowired
	public WebhookUrlValidator(@Value("${gateway.webhook-receiver-base-url:}") String testReceiverBaseUrl) {
		this(InetAddress::getAllByName, testReceiverBaseUrl);
	}

	// Usado pelos testes, para eles não dependerem da rede.
	WebhookUrlValidator(HostResolver resolver, String testReceiverBaseUrl) {
		this.resolver = resolver;
		this.testReceiverBaseUrl = testReceiverBaseUrl;
	}

	// Só a URL do receptor de teste do próprio lojista, montada como o WebhookReceiver a devolve, dispensa a
	// validação. A comparação é do texto inteiro: aceitar tudo o que começasse com a URL do receptor deixaria passar
	// um .../webhook-receiver/../../ que leva a outra rota da própria API.
	public void validate(UUID merchantId, String url) {
		if (!testReceiverBaseUrl.isEmpty() && url.equals(testReceiverBaseUrl + "/" + merchantId)) {
			return;
		}
		validate(url);
	}

	// O URI.create é o mesmo que o WebhookSender usa, para os dois enxergarem o mesmo host na URL.
	void validate(String url) {
		URI uri;
		try {
			uri = URI.create(url);
		} catch (IllegalArgumentException e) {
			throw new InvalidWebhookUrlException("A URL do webhook não é válida.");
		}
		if (!"https".equalsIgnoreCase(uri.getScheme())) {
			throw new InvalidWebhookUrlException("A URL do webhook precisa usar https.");
		}
		// Usuário e senha ficariam gravados em claro no banco. Eles também permitem o truque de
		// https://seubanco.com@10.0.0.1, em que o host de verdade é o que vem depois do @.
		if (uri.getRawUserInfo() != null) {
			throw new InvalidWebhookUrlException("A URL do webhook não pode ter usuário e senha.");
		}
		if (uri.getHost() == null) {
			throw new InvalidWebhookUrlException("A URL do webhook precisa ter um host válido.");
		}
		InetAddress[] addresses;
		try {
			addresses = resolver.resolve(uri.getHost());
		} catch (UnknownHostException e) {
			throw new InvalidWebhookUrlException("O host da URL do webhook não foi encontrado.");
		}
		// Basta um endereço interno para recusar, porque o envio pode usar qualquer um deles.
		for (InetAddress address : addresses) {
			if (!isPublic(address)) {
				throw new InvalidWebhookUrlException("O host da URL do webhook aponta para um endereço que não é público.");
			}
		}
	}

	private static boolean isPublic(InetAddress address) {
		if (address instanceof Inet4Address) {
			int ip = ByteBuffer.wrap(address.getAddress()).getInt();
			return NON_PUBLIC_IPV4.stream().noneMatch(range -> range.contains(ip));
		}
		// No IPv6 é mais seguro dizer o que é público do que listar o que não é. A internet usa só o bloco 2000::/3, e
		// fora dele ficam o loopback ::1, o link-local fe80::/10 e as redes privadas fc00::/7, onde está o
		// fd00:ec2::254, o serviço de metadados da EC2 em IPv6.
		return (address.getAddress()[0] & 0xE0) == 0x20;
	}

	private record Ipv4Range(int network, int mask) {

		static Ipv4Range parse(String cidr) {
			String[] parts = cidr.split("[./]");
			int network = 0;
			for (int i = 0; i < 4; i++) {
				network = (network << 8) | Integer.parseInt(parts[i]);
			}
			return new Ipv4Range(network, -1 << (32 - Integer.parseInt(parts[4])));
		}

		boolean contains(int ip) {
			return (ip & mask) == network;
		}
	}
}
