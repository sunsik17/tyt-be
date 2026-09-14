package com.tyt.auth.infrastructure.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tyt.auth.infrastructure.jwt.JwtTokenAdapter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 유효한 access 토큰이면 userId(Long)를 인증 주체로 둔다.
 * 토큰이 없거나 유효하지 않으면 인증하지 않고 넘기며, 거절은 SecurityConfig의 인가 규칙이 한다.
 *
 * 빈으로 등록하지 않는다. @Component로 두면 Boot가 서블릿 필터로도 자동 등록해 두 번 실행된다.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenAdapter jwtTokenAdapter;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
			jwtTokenAdapter.parseAccessToken(authorization.substring(BEARER_PREFIX.length()))
				.ifPresent(userId -> SecurityContextHolder.getContext().setAuthentication(
					new UsernamePasswordAuthenticationToken(userId, null, List.of())));
		}

		filterChain.doFilter(request, response);
	}
}
