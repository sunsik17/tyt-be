package com.tyt.auth.infrastructure.redis;

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
}
