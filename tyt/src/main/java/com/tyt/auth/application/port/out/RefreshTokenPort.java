package com.tyt.auth.application.port.out;

import java.util.Optional;

public interface RefreshTokenPort {

	/**
	 * 사용자당 하나만 유지한다. 새로 저장하면 이전 refresh 토큰은 무효가 된다.
	 */
	void save(Long userId, String refreshToken);

	/**
	 * 저장된 refresh 토큰을 꺼내면서 지운다. 원자적이라 같은 토큰으로 동시에 재발급해도 하나만 성공한다.
	 */
	Optional<String> consume(Long userId);
}
