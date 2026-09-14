package com.tyt.auth.infrastructure.redis;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.tyt.auth.infrastructure.config.JwtProperties;

/**
 * 실제 Redis 컨테이너로 검증한다. Docker가 없으면 건너뛴다.
 */
@Testcontainers(disabledWithoutDocker = true)
class RefreshTokenRedisAdapterTest {

	private static final Duration REFRESH_VALIDITY = Duration.ofDays(14);
	private static final String KEY = "refresh:1";

	@Container
	private static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

	private static LettuceConnectionFactory connectionFactory;

	private StringRedisTemplate redisTemplate;
	private RefreshTokenRedisAdapter refreshTokenRedisAdapter;

	@BeforeAll
	static void connect() {
		connectionFactory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
		connectionFactory.afterPropertiesSet();
	}

	@AfterAll
	static void disconnect() {
		connectionFactory.destroy();
	}

	@BeforeEach
	void setUp() {
		redisTemplate = new StringRedisTemplate(connectionFactory);
		refreshTokenRedisAdapter = new RefreshTokenRedisAdapter(redisTemplate,
			new JwtProperties("unused-secret-unused-secret-unused-secret", Duration.ofMinutes(30), REFRESH_VALIDITY));
		redisTemplate.delete(KEY);
	}

	@DisplayName("저장한 refresh 토큰을 꺼내면 값이 나오고 지워진다")
	@Test
	void consume() {
		refreshTokenRedisAdapter.save(1L, "refresh-token");

		assertThat(refreshTokenRedisAdapter.consume(1L)).contains("refresh-token");
		assertThat(refreshTokenRedisAdapter.consume(1L)).isEmpty();
	}

	@DisplayName("새로 저장하면 이전 refresh 토큰을 덮어쓴다")
	@Test
	void saveOverwritesPrevious() {
		refreshTokenRedisAdapter.save(1L, "old-refresh");
		refreshTokenRedisAdapter.save(1L, "new-refresh");

		assertThat(refreshTokenRedisAdapter.consume(1L)).contains("new-refresh");
	}

	@DisplayName("refresh 토큰 유효기간으로 만료 시간을 건다")
	@Test
	void saveWithTtl() {
		refreshTokenRedisAdapter.save(1L, "refresh-token");

		Long ttlSeconds = redisTemplate.getExpire(KEY, TimeUnit.SECONDS);
		assertThat(ttlSeconds).isBetween(REFRESH_VALIDITY.minusMinutes(1).toSeconds(), REFRESH_VALIDITY.toSeconds());
	}

	@DisplayName("저장된 토큰이 없으면 비어 있다")
	@Test
	void consumeNothing() {
		assertThat(refreshTokenRedisAdapter.consume(1L)).isEmpty();
	}
}
