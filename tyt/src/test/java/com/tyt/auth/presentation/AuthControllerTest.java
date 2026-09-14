package com.tyt.auth.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tyt.auth.application.AuthCommandService;
import com.tyt.auth.application.dto.command.KakaoLoginCommand;
import com.tyt.auth.application.dto.result.TokenResult;
import com.tyt.auth.domain.exception.AuthErrorCode;
import com.tyt.auth.infrastructure.config.SecurityConfig;
import com.tyt.auth.infrastructure.jwt.JwtTokenAdapter;
import com.tyt.auth.presentation.errorhandler.JwtAuthenticationEntryPoint;
import com.tyt.common.exception.BusinessException;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class})
class AuthControllerTest {

	private static final String URL = "/api/v1/auth/kakao/tokens";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthCommandService authCommandService;

	@MockitoBean
	private JwtTokenAdapter jwtTokenAdapter;

	@DisplayName("토큰 없이 카카오 로그인에 성공하면 200과 토큰을 응답한다")
	@Test
	void loginWithKakao() throws Exception {
		given(authCommandService.loginWithKakao(new KakaoLoginCommand("kakao-token")))
			.willReturn(new TokenResult("access", "refresh"));

		mockMvc.perform(post(URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"accessToken\":\"kakao-token\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("LOGIN_SUCCESS"))
			.andExpect(jsonPath("$.data.accessToken").value("access"))
			.andExpect(jsonPath("$.data.refreshToken").value("refresh"));
	}

	@DisplayName("카카오 토큰이 비어 있으면 400")
	@Test
	void loginWithBlankToken() throws Exception {
		mockMvc.perform(post(URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"accessToken\":\" \"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	@DisplayName("카카오 토큰이 유효하지 않으면 401")
	@Test
	void loginWithInvalidToken() throws Exception {
		given(authCommandService.loginWithKakao(new KakaoLoginCommand("kakao-token")))
			.willThrow(new BusinessException(AuthErrorCode.INVALID_SOCIAL_TOKEN));

		mockMvc.perform(post(URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"accessToken\":\"kakao-token\"}"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_SOCIAL_TOKEN"));
	}
}
