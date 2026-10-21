package com.lucas.gateway.devlogs;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.lucas.gateway.auth.ApiKeyAuthenticationFilter;
import com.lucas.gateway.shared.RequestIdFilter;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

// O appender é registrado por código, e não num logback-spring.xml, por dois motivos: o resto da configuração de log
// continua sendo a padrão do Spring Boot, e ele só existe com o profile demo, como o MerchantLogStream de que depende.
// O Unsynchronized evita que todas as threads disputem um lock a cada linha de log; o MerchantLogStream já cuida da
// concorrência do que é dele.
@Component
@Profile("demo")
public class MerchantLogAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

	// Do framework, só passa o SQL, para quem testa ver o que cada garantia faz no banco. O resto, como o pool de
	// conexões e o Tomcat, mostraria detalhes da infraestrutura a quem só tem uma API key.
	private static final List<String> STREAMED_LOGGERS = List.of(
			"com.lucas.gateway.", "org.hibernate.SQL", "org.springframework.jdbc.core.JdbcTemplate");

	private final MerchantLogStream stream;
	private final Logger rootLogger;

	public MerchantLogAppender(MerchantLogStream stream) {
		this.stream = stream;
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		this.rootLogger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		setContext(context);
		setName("merchant-log-stream");
	}

	@PostConstruct
	void attachToRootLogger() {
		start();
		rootLogger.addAppender(this);
	}

	@PreDestroy
	void detachFromRootLogger() {
		rootLogger.detachAppender(this);
		stop();
	}

	// Linhas sem lojista no MDC, como as da inicialização ou de um pedido sem API key válida, não vão para ninguém.
	// Vai só a mensagem formatada, sem stack trace.
	@Override
	protected void append(ILoggingEvent event) {
		String merchantId = event.getMDCPropertyMap().get(ApiKeyAuthenticationFilter.MDC_KEY);
		if (merchantId == null || !isStreamed(event.getLoggerName())) {
			return;
		}
		stream.publish(UUID.fromString(merchantId), new LogLine(event.getInstant(), event.getLevel().toString(),
				event.getLoggerName(), event.getMDCPropertyMap().get(RequestIdFilter.MDC_KEY),
				event.getFormattedMessage()));
	}

	private static boolean isStreamed(String loggerName) {
		return STREAMED_LOGGERS.stream().anyMatch(loggerName::startsWith);
	}
}
