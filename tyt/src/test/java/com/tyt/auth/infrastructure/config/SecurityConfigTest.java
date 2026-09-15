package com.tyt.auth.infrastructure.config;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import com.tyt.auth.infrastructure.jwt.JwtTokenAdapter;
import com.tyt.auth.presentation.AuthController;
import com.tyt.auth.presentation.errorhandler.JwtAuthenticationEntryPoint;

/**
 * 인가 규칙과 401 응답 형식을 검증한다. 보호된 경로의 컨트롤러는 슬라이스에 올리지 않았으므로,
 * 인증을 통과하면 404가 된다.
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class})
class SecurityConfigTest {

	private static final String PROTECTED_URL = "/api/v1/users/me";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthCommandService authCommandService;

	@MockitoBean
	private JwtTokenAdapter jwtTokenAdapter;

	@DisplayName("토큰 없이 보호된 경로를 호출하면 401 UNAUTHENTICATED")
	@Test
	void protectedPathWithoutToken() throws Exception {
		mockMvc.perform(get(PROTECTED_URL))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
	}

	@DisplayName("access 토큰으로 인정되지 않는 토큰이면 401 UNAUTHENTICATED")
	@Test
	void protectedPathWithInvalidToken() throws Exception {
		given(jwtTokenAdapter.parseAccessToken("refresh-token")).willReturn(Optional.empty());

		mockMvc.perform(get(PROTECTED_URL).header(HttpHeaders.AUTHORIZATION, "Bearer refresh-token"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
	}

	@DisplayName("유효한 access 토큰이면 인증을 통과한다")
	@Test
	void protectedPathWithValidToken() throws Exception {
		given(jwtTokenAdapter.parseAccessToken("access-token")).willReturn(Optional.of(1L));

		mockMvc.perform(get(PROTECTED_URL).header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
			.andExpect(status().isNotFound());
	}

	@DisplayName("로그인과 재발급은 토큰 없이 호출할 수 있다")
	@Test
	void loginAndReissueArePublic() throws Exception {
		mockMvc.perform(post("/api/v1/auth/kakao/tokens").contentType(MediaType.APPLICATION_JSON).content("{}"))
			.andExpect(status().isBadRequest());
		mockMvc.perform(post("/api/v1/auth/tokens").contentType(MediaType.APPLICATION_JSON).content("{}"))
			.andExpect(status().isBadRequest());
	}

	@DisplayName("같은 auth 경로라도 로그아웃과 탈퇴는 토큰이 없으면 401")
	@Test
	void logoutAndWithdrawRequireToken() throws Exception {
		mockMvc.perform(delete("/api/v1/auth/tokens"))
			.andExpect(status().isUnauthorized());
		mockMvc.perform(delete("/api/v1/auth/accounts/me"))
			.andExpect(status().isUnauthorized());
	}
}
