package com.tyt.auth.infrastructure.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.tyt.auth.application.dto.result.TokenResult;
import com.tyt.auth.application.port.out.AuthTokenPort;
import com.tyt.auth.infrastructure.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenAdapter implements AuthTokenPort {

	/**
	 * refresh 토큰을 access 토큰 자리에 넣어 인증하는 것을 막기 위해 종류를 담는다.
	 */
	private static final String TYPE_CLAIM = "type";
	private static final String ACCESS_TYPE = "access";
	private static final String REFRESH_TYPE = "refresh";

	private final JwtProperties jwtProperties;
	private final SecretKey secretKey;

	public JwtTokenAdapter(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public TokenResult issue(Long userId) {
		Date now = new Date();

		return new TokenResult(
			createToken(userId, ACCESS_TYPE, now, jwtProperties.accessTokenValidity()),
			createToken(userId, REFRESH_TYPE, now, jwtProperties.refreshTokenValidity())
		);
	}

	/**
	 * 유효한 access 토큰이면 userId를 돌려준다.
	 * 서명 불일치, 만료, 형식 오류, refresh 토큰이면 비어 있다.
	 */
	public Optional<Long> parseAccessToken(String token) {
		try {
			Claims claims = Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();

			if (!ACCESS_TYPE.equals(claims.get(TYPE_CLAIM, String.class))) {
				return Optional.empty();
			}
			return Optional.of(Long.valueOf(claims.getSubject()));
		} catch (JwtException | IllegalArgumentException e) {
			return Optional.empty();
		}
	}

	private String createToken(Long userId, String type, Date issuedAt, Duration validity) {
		return Jwts.builder()
			.subject(String.valueOf(userId))
			.claim(TYPE_CLAIM, type)
			.issuedAt(issuedAt)
			.expiration(new Date(issuedAt.getTime() + validity.toMillis()))
			.signWith(secretKey)
			.compact();
	}
}
