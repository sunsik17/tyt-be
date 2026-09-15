package com.tyt.auth.presentation.dto.response;

import com.tyt.auth.application.dto.result.TokenResult;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponse(
	@Schema(description = "API 호출에 쓰는 JWT. `Authorization: Bearer`에 넣는다. 유효기간 30분", example = "eyJhbGciOiJIUzI1NiJ9.access")
	String accessToken,

	@Schema(description = "재발급에만 쓰는 JWT. 유효기간 14일. 재발급하면 새 값으로 바뀌고 이전 값은 무효", example = "eyJhbGciOiJIUzI1NiJ9.refresh")
	String refreshToken
) {

	public static TokenResponse from(TokenResult result) {
		return new TokenResponse(result.accessToken(), result.refreshToken());
	}
}
