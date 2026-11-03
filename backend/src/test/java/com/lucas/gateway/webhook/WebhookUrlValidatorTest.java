package com.lucas.gateway.webhook;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WebhookUrlValidatorTest {

	private static final Map<String, List<String>> FAKE_DNS = Map.of(
			"example.com", List.of("93.184.215.14", "2606:2800:21f:cb07:6820:80da:af6b:8b2c"),
			"localhost", List.of("127.0.0.1", "::1"),
			"intranet.example", List.of("10.0.0.5"),
			"metadados.example", List.of("169.254.169.254"),
			"meio-interno.example", List.of("93.184.215.14", "10.0.0.5"));

	private final WebhookUrlValidator validator = new WebhookUrlValidator(WebhookUrlValidatorTest::resolve);

	@ParameterizedTest
	@ValueSource(strings = {
			"https://example.com/webhook",
			"https://example.com:8443/hooks?loja=1",
			"HTTPS://example.com/webhook",
			"https://93.184.215.14/webhook",
			"https://[2606:2800:21f:cb07:6820:80da:af6b:8b2c]/webhook",
			// Vizinhos das faixas recusadas, para conferir que as máscaras não pegam endereço a mais.
			"https://172.15.255.255/", "https://172.32.0.0/", "https://100.63.255.255/", "https://100.128.0.0/",
			"https://11.0.0.0/", "https://9.255.255.255/"})
	void acceptsHttpsUrlsThatOnlyReachPublicAddresses(String url) {
		assertThatCode(() -> validator.validate(url)).doesNotThrowAnyException();
	}

	@ParameterizedTest(name = "{0}")
	@CsvSource(delimiter = '|', textBlock = """
			http://example.com/webhook                | precisa usar https
			ftp://example.com/webhook                 | precisa usar https
			https://usuario:senha@example.com/webhook | usuário e senha
			https://example.com@10.0.0.1/webhook      | usuário e senha
			https:///webhook                          | host válido
			https://exa mple.com/webhook              | não é válida
			https://nao-existe.example/webhook        | não foi encontrado
			https://localhost/webhook                 | não é público
			https://127.0.0.1/webhook                 | não é público
			https://0.0.0.0/webhook                   | não é público
			https://10.0.0.1/webhook                  | não é público
			https://172.16.0.0/webhook                | não é público
			https://172.31.255.255/webhook            | não é público
			https://192.168.1.1/webhook               | não é público
			https://100.64.0.1/webhook                | não é público
			https://169.254.169.254/latest/meta-data/ | não é público
			https://224.0.0.1/webhook                 | não é público
			https://255.255.255.255/webhook           | não é público
			https://[::1]/webhook                     | não é público
			https://[::]/webhook                      | não é público
			https://[fe80::1]/webhook                 | não é público
			https://[fd00:ec2::254]/webhook           | não é público
			# Endereços internos escritos de outro jeito.
			https://2130706433/webhook                | não é público
			https://[::ffff:127.0.0.1]/webhook        | não é público
			https://[::ffff:169.254.169.254]/webhook  | não é público
			https://[::127.0.0.1]/webhook             | não é público
			# Nomes que resolvem para dentro, inclusive um que só tem um dos endereços interno.
			https://intranet.example/webhook          | não é público
			https://metadados.example/webhook         | não é público
			https://meio-interno.example/webhook      | não é público
			""")
	void rejects(String url, String reason) {
		assertThatThrownBy(() -> validator.validate(url))
				.isInstanceOf(InvalidWebhookUrlException.class)
				.hasMessageContaining(reason);
	}

	// Os nomes vêm do mapa, e qualquer outro dá "não encontrado". Um IP escrito na URL vai para o InetAddress, que
	// só converte o texto, sem consultar a rede.
	private static InetAddress[] resolve(String host) throws UnknownHostException {
		List<String> ips = FAKE_DNS.get(host);
		if (ips != null) {
			InetAddress[] addresses = new InetAddress[ips.size()];
			for (int i = 0; i < ips.size(); i++) {
				addresses[i] = InetAddress.getByName(ips.get(i));
			}
			return addresses;
		}
		if (host.startsWith("[") || host.matches("[\\d.]+")) {
			return InetAddress.getAllByName(host);
		}
		throw new UnknownHostException(host);
	}
}
