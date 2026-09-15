package com.tyt.common.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * 모든 API는 기본으로 bearer 토큰을 요구하는 것으로 문서화한다.
 * 토큰 없이 호출하는 컨트롤러는 @SecurityRequirements로 이 요구를 지운다.
 */
@Configuration
public class OpenApiConfig {

	private static final String BEARER_AUTH = "bearerAuth";

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("TYT API")
				.version("v1")
				.description("로그인, 토큰 저장, 401 처리처럼 여러 호출에 걸친 흐름은 tyt-be의 docs/api-guide.md를 본다."))
			// 지정하지 않으면 스펙을 생성한 테스트의 MockMvc 주소(포트 없는 localhost)가 들어간다
			.servers(List.of(new Server().url("http://localhost:8080").description("로컬")))
			.components(new Components()
				.addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
					.type(SecurityScheme.Type.HTTP)
					.scheme("bearer")
					.bearerFormat("JWT")
					.description("POST /api/v1/auth/kakao/tokens 또는 POST /api/v1/auth/tokens로 받은 accessToken")))
			.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
	}
}
