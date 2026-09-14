package com.tyt.auth.infrastructure.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * secret은 HS256 서명용이라 32바이트 이상이어야 한다.
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
	String secret,
	Duration accessTokenValidity,
	Duration refreshTokenValidity
) {
}
