package com.tyt.auth.domain.exception;

import com.tyt.common.exception.ErrorCode;
import com.tyt.common.exception.ErrorType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

	INVALID_SOCIAL_TOKEN(ErrorType.UNAUTHORIZED, "소셜 로그인 토큰이 유효하지 않습니다."),
	UNAUTHENTICATED(ErrorType.UNAUTHORIZED, "인증이 필요합니다."),
	INVALID_REFRESH_TOKEN(ErrorType.UNAUTHORIZED, "refresh 토큰이 유효하지 않습니다. 다시 로그인해야 합니다."),
	SOCIAL_UNLINK_FAILED(ErrorType.INTERNAL, "카카오 연결 끊기에 실패했습니다. 잠시 후 다시 시도해주세요."),
	INVALID_SOCIAL_ACCOUNT(ErrorType.INVALID, "소셜 계정 정보가 올바르지 않습니다.");

	private final ErrorType errorType;
	private final String message;
}
