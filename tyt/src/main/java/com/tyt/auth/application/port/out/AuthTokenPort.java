package com.tyt.auth.application.port.out;

import java.util.Optional;

import com.tyt.auth.application.dto.result.TokenResult;

public interface AuthTokenPort {

	TokenResult issue(Long userId);

	/**
	 * 유효한 refresh 토큰이면 userId를 돌려준다.
	 * 서명 불일치, 만료, 형식 오류, access 토큰이면 비어 있다.
	 */
	Optional<Long> parseRefreshToken(String refreshToken);
}
