package com.tyt.auth.presentation.dto.response;

import com.tyt.auth.application.dto.result.TokenResult;

public record TokenResponse(
	String accessToken,
	String refreshToken
) {

	public static TokenResponse from(TokenResult result) {
		return new TokenResponse(result.accessToken(), result.refreshToken());
	}
}
