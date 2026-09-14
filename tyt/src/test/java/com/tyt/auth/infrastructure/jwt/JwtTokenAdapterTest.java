package com.tyt.auth.infrastructure.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tyt.auth.application.dto.result.TokenResult;
import com.tyt.auth.infrastructure.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtTokenAdapterTest {

	private static final String SECRET = "test-secret-key-must-be-at-least-32-bytes-long";
	private static final Duration ACCESS_VALIDITY = Duration.ofMinutes(30);
	private static final Duration REFRESH_VALIDITY = Duration.ofDays(14);

	private final JwtTokenAdapter jwtTokenAdapter =
		new JwtTokenAdapter(new JwtProperties(SECRET, ACCESS_VALIDITY, REFRESH_VALIDITY));
	private final SecretKey secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

	@DisplayName("access와 refresh 모두 userId를 subject로 담는다")
	@Test
	void subject() {
		TokenResult tokens = jwtTokenAdapter.issue(1L);

		assertThat(parse(tokens.accessToken()).getSubject()).isEqualTo("1");
		assertThat(parse(tokens.refreshToken()).getSubject()).isEqualTo("1");
	}

	@DisplayName("access와 refresh는 type 클레임으로 구분된다")
	@Test
	void type() {
		TokenResult tokens = jwtTokenAdapter.issue(1L);

		assertThat(parse(tokens.accessToken()).get("type", String.class)).isEqualTo("access");
		assertThat(parse(tokens.refreshToken()).get("type", String.class)).isEqualTo("refresh");
	}

	@DisplayName("만료 시각은 발급 시각에 각 유효기간을 더한 값이다")
	@Test
	void expiration() {
		TokenResult tokens = jwtTokenAdapter.issue(1L);
		Claims access = parse(tokens.accessToken());
		Claims refresh = parse(tokens.refreshToken());

		assertThat(access.getExpiration().getTime() - access.getIssuedAt().getTime())
			.isEqualTo(ACCESS_VALIDITY.toMillis());
		assertThat(refresh.getExpiration().getTime() - refresh.getIssuedAt().getTime())
			.isEqualTo(REFRESH_VALIDITY.toMillis());
	}

	private Claims parse(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}
}
