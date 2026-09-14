package com.tyt.auth.application.port.out;

public interface RefreshTokenPort {

	/**
	 * 사용자당 하나만 유지한다. 새로 저장하면 이전 refresh 토큰은 무효가 된다.
	 */
	void save(Long userId, String refreshToken);
}
