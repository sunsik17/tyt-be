package com.tyt.auth.presentation.errorhandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.tyt.auth.domain.exception.AuthErrorCode;
import com.tyt.common.web.ApiResponse;
import com.tyt.common.web.HttpStatusResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

/**
 * 인증 실패는 필터 단계에서 일어나 GlobalExceptionHandler에 닿지 않는다.
 * 그래서 같은 ApiResponse 형식을 여기서 직접 쓴다.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException authException) throws IOException {

		response.setStatus(HttpStatusResolver.resolve(AuthErrorCode.UNAUTHENTICATED.getErrorType()).value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		objectMapper.writeValue(response.getOutputStream(), ApiResponse.error(AuthErrorCode.UNAUTHENTICATED));
	}
}
