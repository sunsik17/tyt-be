package com.tyt.auth.presentation.dto.request;

import com.tyt.auth.application.dto.command.KakaoLoginCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record KakaoLoginRequest(
	@Schema(description = "카카오 SDK 로그인으로 받은 카카오 액세스 토큰", example = "kakao-sdk-access-token")
	@NotBlank String accessToken
) {

	public KakaoLoginCommand toCommand() {
		return new KakaoLoginCommand(accessToken);
	}
}
