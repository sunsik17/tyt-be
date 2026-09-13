package com.tyt.common.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.tyt.common.exception.BusinessException;
import com.tyt.common.exception.CommonErrorCode;
import com.tyt.common.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

/**
 * ResponseEntityExceptionHandler를 상속해 Spring MVC 표준 예외(헤더·파라미터 누락,
 * 타입 불일치, 405, 415 등)가 catch-all로 떨어져 500이 되는 것을 막는다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
		ErrorCode errorCode = e.getErrorCode();

		return ResponseEntity
			.status(HttpStatusResolver.resolve(errorCode.getErrorType()))
			.body(ApiResponse.error(errorCode));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
		log.error("처리되지 않은 예외", e);

		return ResponseEntity
			.status(HttpStatusResolver.resolve(CommonErrorCode.INTERNAL_ERROR.getErrorType()))
			.body(ApiResponse.error(CommonErrorCode.INTERNAL_ERROR));
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
		HttpHeaders headers, HttpStatusCode status, WebRequest request) {

		FieldError fieldError = e.getBindingResult().getFieldError();
		String message = fieldError == null
			? CommonErrorCode.INVALID_REQUEST.getMessage()
			: fieldError.getField() + ": " + fieldError.getDefaultMessage();

		return ResponseEntity.status(status).body(ApiResponse.error(CommonErrorCode.INVALID_REQUEST, message));
	}

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception e, Object body,
		HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {

		ErrorCode errorCode = statusCode.is5xxServerError()
			? CommonErrorCode.INTERNAL_ERROR
			: CommonErrorCode.INVALID_REQUEST;

		return ResponseEntity.status(statusCode).body(ApiResponse.error(errorCode, e.getMessage()));
	}
}
