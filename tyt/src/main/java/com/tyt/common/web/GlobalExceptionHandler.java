package com.tyt.common.web;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.tyt.common.exception.BusinessException;
import com.tyt.common.exception.CommonErrorCode;
import com.tyt.common.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
		ErrorCode errorCode = e.getErrorCode();
		return ResponseEntity
			.status(HttpStatusResolver.resolve(errorCode.getErrorType()))
			.body(ApiResponse.error(errorCode));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
		FieldError fieldError = e.getBindingResult().getFieldError();
		String message = fieldError == null
			? CommonErrorCode.INVALID_REQUEST.getMessage()
			: fieldError.getField() + ": " + fieldError.getDefaultMessage();

		return ResponseEntity
			.status(HttpStatusResolver.resolve(CommonErrorCode.INVALID_REQUEST.getErrorType()))
			.body(ApiResponse.error(CommonErrorCode.INVALID_REQUEST, message));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
		log.error("처리되지 않은 예외", e);
		return ResponseEntity
			.status(HttpStatusResolver.resolve(CommonErrorCode.INTERNAL_ERROR.getErrorType()))
			.body(ApiResponse.error(CommonErrorCode.INTERNAL_ERROR));
	}
}
