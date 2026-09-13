package com.tyt.user.domain.exception;

import com.tyt.common.exception.ErrorCode;
import com.tyt.common.exception.ErrorType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

	USER_NOT_FOUND(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다.");

	private final ErrorType errorType;
	private final String message;
}
