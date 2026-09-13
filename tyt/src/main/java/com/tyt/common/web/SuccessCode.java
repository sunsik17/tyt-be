package com.tyt.common.web;

public interface SuccessCode {

	String getMessage();

	/**
	 * enum 상수 이름이 곧 클라이언트에 내려가는 성공 코드가 된다.
	 */
	String name();
}
