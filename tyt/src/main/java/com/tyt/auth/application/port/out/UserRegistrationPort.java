package com.tyt.auth.application.port.out;

public interface UserRegistrationPort {

	/**
	 * 사용자를 가입시키고 id를 돌려준다.
	 */
	Long register();
}
