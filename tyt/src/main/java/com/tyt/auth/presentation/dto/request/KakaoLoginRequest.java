package com.tyt.auth.presentation.dto.request;

import com.tyt.auth.application.dto.command.KakaoLoginCommand;

import jakarta.validation.constraints.NotBlank;

public record KakaoLoginRequest(
	@NotBlank String accessToken
) {

	public KakaoLoginCommand toCommand() {
		return new KakaoLoginCommand(accessToken);
	}
}
