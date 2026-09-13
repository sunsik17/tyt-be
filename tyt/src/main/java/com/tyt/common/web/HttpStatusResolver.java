package com.tyt.common.web;

import org.springframework.http.HttpStatus;

import com.tyt.common.exception.ErrorType;

/**
 * ErrorType을 HttpStatus로 옮기는 유일한 지점.
 * domain은 전달 방식을 모르므로 이 변환은 web 계층에만 둔다.
 */
public final class HttpStatusResolver {

	private HttpStatusResolver() {
	}

	public static HttpStatus resolve(ErrorType errorType) {
		return switch (errorType) {
			case INVALID -> HttpStatus.BAD_REQUEST;
			case NOT_FOUND -> HttpStatus.NOT_FOUND;
			case CONFLICT -> HttpStatus.CONFLICT;
			case FORBIDDEN -> HttpStatus.FORBIDDEN;
			case INTERNAL -> HttpStatus.INTERNAL_SERVER_ERROR;
		};
	}
}
