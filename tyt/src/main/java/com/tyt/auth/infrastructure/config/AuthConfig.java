package com.tyt.auth.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({KakaoProperties.class, JwtProperties.class})
public class AuthConfig {

	private static final String KAKAO_API_BASE_URL = "https://kapi.kakao.com";

	@Bean
	public RestClient kakaoRestClient() {
		return RestClient.builder()
			.baseUrl(KAKAO_API_BASE_URL)
			.build();
	}
}
