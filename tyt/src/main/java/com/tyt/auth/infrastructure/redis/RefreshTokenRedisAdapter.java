package com.tyt.auth.infrastructure.redis;

import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.tyt.auth.application.port.out.RefreshTokenPort;
import com.tyt.auth.infrastructure.config.JwtProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RefreshTokenRedisAdapter implements RefreshTokenPort {

	private static final String KEY_PREFIX = "refresh:";

	private final StringRedisTemplate redisTemplate;
	private final JwtProperties jwtProperties;

	@Override
	public void save(Long userId, String refreshToken) {
		redisTemplate.opsForValue().set(KEY_PREFIX + userId, refreshToken, jwtProperties.refreshTokenValidity());
	}

	/**
	 * GETDEL로 꺼내면서 지운다. Redis 6.2 이상이 필요하다.
	 */
	@Override
	public Optional<String> consume(Long userId) {
		return Optional.ofNullable(redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + userId));
	}
}
