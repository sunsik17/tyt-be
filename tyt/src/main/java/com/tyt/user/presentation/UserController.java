package com.tyt.user.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tyt.common.web.ApiResponse;
import com.tyt.user.application.UserQueryService;
import com.tyt.user.application.dto.result.UserResult;
import com.tyt.user.presentation.dto.response.UserResponse;
import com.tyt.user.presentation.dto.response.constants.UserSuccessCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	/**
	 * 임시. 인증이 붙으면 JWT에서 userId를 꺼내고 이 헤더는 없앤다.
	 */
	private static final String TEMP_USER_ID_HEADER = "X-User-Id";

	private final UserQueryService userQueryService;

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<UserResponse>> getMe(@RequestHeader(TEMP_USER_ID_HEADER) Long userId) {
		UserResult result = userQueryService.getById(userId);

		return ResponseEntity.ok(ApiResponse.success(UserSuccessCode.USER_FOUND, UserResponse.from(result)));
	}
}
