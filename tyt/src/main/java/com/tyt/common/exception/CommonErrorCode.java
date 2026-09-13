package com.tyt.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

	INVALID_REQUEST(ErrorType.INVALID, "요청 형식이 올바르지 않습니다."),
	INTERNAL_ERROR(ErrorType.INTERNAL, "서버 오류가 발생했습니다.");

	private final ErrorType errorType;
	private final String message;
}
