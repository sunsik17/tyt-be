package com.tyt.auth.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.tyt.auth.infrastructure.jwt.JwtTokenAdapter;
import com.tyt.auth.infrastructure.security.JwtAuthenticationFilter;
import com.tyt.auth.presentation.errorhandler.JwtAuthenticationEntryPoint;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtTokenAdapter jwtTokenAdapter;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorize -> authorize
				// 토큰 없이 호출하는 API는 로그인과 재발급뿐이다. 같은 prefix의 로그아웃·탈퇴는 인증이 필요하다
				.requestMatchers(HttpMethod.POST, "/api/v1/auth/kakao/tokens", "/api/v1/auth/tokens").permitAll()
				// API 문서. 운영 프로파일에서는 springdoc을 꺼서 이 경로 자체가 없어진다
				.requestMatchers("/v3/api-docs/**", "/v3/api-docs.yaml", "/swagger-ui/**", "/swagger-ui.html").permitAll()
				.anyRequest().authenticated())
			.exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
			.addFilterBefore(new JwtAuthenticationFilter(jwtTokenAdapter), UsernamePasswordAuthenticationFilter.class)
			.build();
	}
}
