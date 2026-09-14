package com.tyt.user.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tyt.common.exception.BusinessException;
import com.tyt.user.application.UserQueryService;
import com.tyt.user.application.dto.result.UserResult;
import com.tyt.user.domain.exception.UserErrorCode;

/**
 * 인증 필터와 인가 규칙은 auth 도메인이 소유하고 거기서 검증한다.
 * 여기서는 필터를 끄고 인증 주체만 직접 넣어, user가 auth를 모르는 상태로 컨트롤러를 검증한다.
 */
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserQueryService userQueryService;

	@BeforeEach
	void authenticate() {
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(1L, null, List.of()));
	}

	@AfterEach
	void clearAuthentication() {
		SecurityContextHolder.clearContext();
	}

	@DisplayName("인증 주체의 정보를 조회하면 200과 ApiResponse 형식으로 응답한다")
	@Test
	void getMe() throws Exception {
		given(userQueryService.getById(1L))
			.willReturn(new UserResult(1L, LocalDateTime.of(2026, 9, 14, 0, 0)));

		mockMvc.perform(get("/api/v1/users/me"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("USER_FOUND"))
			.andExpect(jsonPath("$.data.id").value(1))
			.andExpect(jsonPath("$.data.createdAt").value("2026-09-14T00:00:00"));
	}

	@DisplayName("없는 사용자면 404와 에러 코드로 응답한다")
	@Test
	void getMeNotFound() throws Exception {
		given(userQueryService.getById(1L))
			.willThrow(new BusinessException(UserErrorCode.USER_NOT_FOUND));

		mockMvc.perform(get("/api/v1/users/me"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
	}
}
