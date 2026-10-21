package com.lucas.gateway.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

// Herdar de ResponseEntityExceptionHandler faz os erros do próprio Spring MVC (JSON malformado,
// rota inexistente, método HTTP errado...) também saírem em Problem Details, sem um handler para cada um.
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(BadRequestException.class)
	public ProblemDetail handleBadRequest(BadRequestException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(NotFoundException.class)
	public ProblemDetail handleNotFound(NotFoundException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	// Os 409 e 422 são as regras de negócio barrando um pedido, como a máquina de estados ou a idempotência, e por isso
	// ficam registrados. As mensagens são todas montadas pela própria API e não trazem nada que o cliente enviou.
	@ExceptionHandler(ConflictException.class)
	public ProblemDetail handleConflict(ConflictException ex) {
		log.info("Recusado com 409: {}", ex.getMessage());
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
	}

	// O @Version da Charge faz o Hibernate gravar com UPDATE ... WHERE version = ?. Se outro pedido alterou a
	// cobrança entre a leitura e a gravação, esse UPDATE não encontra a linha e a transação inteira é desfeita.
	// Isso só aparece no commit, quando o service já terminou, e por isso não dá para transformar em exceção de
	// domínio como as outras. A mensagem é fixa porque a da exceção traz o nome da classe e o id da entidade.
	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	public ProblemDetail handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
		log.info("Recusado com 409 pelo lock otimista: outro pedido gravou a cobrança primeiro, e esta gravação foi desfeita");
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
				"Outro pedido alterou estes dados ao mesmo tempo. Consulte o estado atual e tente de novo.");
	}

	@ExceptionHandler(UnprocessableException.class)
	public ProblemDetail handleUnprocessable(UnprocessableException ex) {
		log.info("Recusado com 422: {}", ex.getMessage());
		return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
	}

	@ExceptionHandler(AuthenticationException.class)
	public ProblemDetail handleUnauthenticated(AuthenticationException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
				"Envie uma API key válida no header X-Api-Key.");
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleUnexpected(Exception ex) {
		// Quando um handler trata a exceção, o Spring deixa de registrá-la no log. Sem esta linha,
		// todo erro inesperado sumiria sem deixar rastro. A mensagem para o cliente é genérica de propósito,
		// para não expor detalhes internos.
		log.error("Erro inesperado", ex);
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
				"Erro interno. Tente novamente mais tarde.");
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		List<InvalidField> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> new InvalidField(error.getField(), error.getDefaultMessage()))
				.toList();

		ProblemDetail problem = ex.getBody();
		problem.setDetail("Um ou mais campos são inválidos.");
		problem.setProperty("errors", errors);
		return handleExceptionInternal(ex, problem, headers, status, request);
	}

	// O handleMethodArgumentNotValid cobre só o corpo do pedido. Quando a validação falha num parâmetro da query
	// string, como o size da listagem de cobranças, o Spring lança esta outra exceção, e a resposta padrão dela
	// diz apenas "Validation failure", sem apontar o parâmetro.
	@Override
	protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		List<InvalidField> errors = ex.getParameterValidationResults().stream()
				.flatMap(result -> result.getResolvableErrors().stream()
						.map(error -> new InvalidField(result.getMethodParameter().getParameterName(),
								error.getDefaultMessage())))
				.toList();

		ProblemDetail problem = ex.getBody();
		problem.setDetail("Um ou mais parâmetros são inválidos.");
		problem.setProperty("errors", errors);
		return handleExceptionInternal(ex, problem, headers, status, request);
	}

	public record InvalidField(String field, String message) {
	}
}
