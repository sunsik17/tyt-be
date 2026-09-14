package com.tyt.auth.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tyt.auth.application.AuthCommandService;
import com.tyt.auth.application.dto.result.TokenResult;
import com.tyt.auth.presentation.dto.request.KakaoLoginRequest;
import com.tyt.auth.presentation.dto.response.TokenResponse;
import com.tyt.auth.presentation.dto.response.constants.AuthSuccessCode;
import com.tyt.common.web.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthCommandService authCommandService;

	@PostMapping("/kakao/tokens")
	public ResponseEntity<ApiResponse<TokenResponse>> loginWithKakao(@Valid @RequestBody KakaoLoginRequest request) {
		TokenResult result = authCommandService.loginWithKakao(request.toCommand());

		return ResponseEntity.ok(ApiResponse.success(AuthSuccessCode.LOGIN_SUCCESS, TokenResponse.from(result)));
	}
}
