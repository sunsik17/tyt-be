package com.tyt.common.exception;

public interface ErrorCode {

	ErrorType getErrorType();

	String getMessage();

	/**
	 * enum 상수 이름이 곧 클라이언트에 내려가는 에러 코드가 된다.
	 */
	String name();
}
