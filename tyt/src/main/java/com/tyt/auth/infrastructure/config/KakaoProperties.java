package com.tyt.auth.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * adminKey는 모든 사용자의 연결을 끊을 수 있는 비밀값이다. 서버에만 두고 앱에 넣지 않는다.
 */
@ConfigurationProperties(prefix = "kakao")
public record KakaoProperties(
	Long appId,
	String adminKey
) {
}
