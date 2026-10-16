package com.lucas.gateway.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

// Herdar de ResponseEntityExceptionHandler faz os erros do próprio Spring MVC (JSON malformado,
// rota inexistente, método HTTP errado...) também saírem em Problem Details, sem um handler para cada um.
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(NotFoundException.class)
	public ProblemDetail handleNotFound(NotFoundException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(ConflictException.class)
	public ProblemDetail handleConflict(ConflictException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
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

	public record InvalidField(String field, String message) {
	}
}
