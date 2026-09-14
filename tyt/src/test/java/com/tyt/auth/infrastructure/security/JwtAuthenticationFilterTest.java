package com.tyt.auth.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.tyt.auth.infrastructure.jwt.JwtTokenAdapter;

class JwtAuthenticationFilterTest {

	private final JwtTokenAdapter jwtTokenAdapter = mock(JwtTokenAdapter.class);
	private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenAdapter);

	private final MockHttpServletRequest request = new MockHttpServletRequest();
	private final MockHttpServletResponse response = new MockHttpServletResponse();
	private final MockFilterChain filterChain = new MockFilterChain();

	@AfterEach
	void clearAuthentication() {
		SecurityContextHolder.clearContext();
	}

	@DisplayName("유효한 access 토큰이면 userId를 인증 주체로 둔다")
	@Test
	void authenticateWithValidToken() throws Exception {
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer access-token");
		given(jwtTokenAdapter.parseAccessToken("access-token")).willReturn(Optional.of(1L));

		filter.doFilter(request, response, filterChain);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		assertThat(authentication.getPrincipal()).isEqualTo(1L);
		assertThat(authentication.isAuthenticated()).isTrue();
		assertThat(filterChain.getRequest()).isNotNull();
	}

	@DisplayName("유효하지 않은 토큰이면 인증하지 않고 다음 필터로 넘긴다")
	@Test
	void skipWithInvalidToken() throws Exception {
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");
		given(jwtTokenAdapter.parseAccessToken("invalid-token")).willReturn(Optional.empty());

		filter.doFilter(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		assertThat(filterChain.getRequest()).isNotNull();
	}

	@DisplayName("Authorization 헤더가 없으면 토큰을 검사하지 않고 넘긴다")
	@Test
	void skipWithoutHeader() throws Exception {
		filter.doFilter(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(jwtTokenAdapter, never()).parseAccessToken(anyString());
		assertThat(filterChain.getRequest()).isNotNull();
	}

	@DisplayName("Bearer 방식이 아니면 토큰을 검사하지 않는다")
	@Test
	void skipWithOtherScheme() throws Exception {
		request.addHeader(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz");

		filter.doFilter(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(jwtTokenAdapter, never()).parseAccessToken(anyString());
	}
}
