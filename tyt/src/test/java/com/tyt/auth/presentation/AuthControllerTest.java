package com.tyt.auth.presentation;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tyt.auth.application.AuthCommandService;
import com.tyt.auth.application.dto.command.KakaoLoginCommand;
import com.tyt.auth.application.dto.command.ReissueTokenCommand;
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
	private static final String REISSUE_URL = "/api/v1/auth/tokens";
	private static final String LOGOUT_URL = "/api/v1/auth/tokens";
	private static final String WITHDRAW_URL = "/api/v1/auth/accounts/me";

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

	@DisplayName("토큰 없이 refresh 토큰으로 재발급에 성공하면 200과 새 토큰을 응답한다")
	@Test
	void reissue() throws Exception {
		given(authCommandService.reissue(new ReissueTokenCommand("old-refresh")))
			.willReturn(new TokenResult("new-access", "new-refresh"));

		mockMvc.perform(post(REISSUE_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"refreshToken\":\"old-refresh\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("TOKEN_REISSUED"))
			.andExpect(jsonPath("$.data.accessToken").value("new-access"))
			.andExpect(jsonPath("$.data.refreshToken").value("new-refresh"));
	}

	@DisplayName("refresh 토큰이 비어 있으면 400")
	@Test
	void reissueWithBlankToken() throws Exception {
		mockMvc.perform(post(REISSUE_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"refreshToken\":\"\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	@DisplayName("refresh 토큰이 유효하지 않으면 401 INVALID_REFRESH_TOKEN")
	@Test
	void reissueWithInvalidToken() throws Exception {
		given(authCommandService.reissue(new ReissueTokenCommand("old-refresh")))
			.willThrow(new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN));

		mockMvc.perform(post(REISSUE_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"refreshToken\":\"old-refresh\"}"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
	}

	@DisplayName("로그아웃하면 200 LOGGED_OUT")
	@Test
	void logout() throws Exception {
		given(jwtTokenAdapter.parseAccessToken("access-token")).willReturn(Optional.of(1L));

		mockMvc.perform(delete(LOGOUT_URL).header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("LOGGED_OUT"));

		verify(authCommandService).logout(1L);
	}

	@DisplayName("토큰 없이 로그아웃하면 401 UNAUTHENTICATED")
	@Test
	void logoutWithoutToken() throws Exception {
		mockMvc.perform(delete(LOGOUT_URL))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
	}

	@DisplayName("탈퇴하면 200 ACCOUNT_DELETED")
	@Test
	void withdraw() throws Exception {
		given(jwtTokenAdapter.parseAccessToken("access-token")).willReturn(Optional.of(1L));

		mockMvc.perform(delete(WITHDRAW_URL).header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("ACCOUNT_DELETED"));

		verify(authCommandService).withdraw(1L);
	}

	@DisplayName("카카오 연결 끊기에 실패하면 500 SOCIAL_UNLINK_FAILED")
	@Test
	void withdrawWhenUnlinkFails() throws Exception {
		given(jwtTokenAdapter.parseAccessToken("access-token")).willReturn(Optional.of(1L));
		willThrow(new BusinessException(AuthErrorCode.SOCIAL_UNLINK_FAILED)).given(authCommandService).withdraw(1L);

		mockMvc.perform(delete(WITHDRAW_URL).header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.code").value("SOCIAL_UNLINK_FAILED"));
	}

	@DisplayName("토큰 없이 탈퇴하면 401 UNAUTHENTICATED")
	@Test
	void withdrawWithoutToken() throws Exception {
		mockMvc.perform(delete(WITHDRAW_URL))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
	}
}
