package com.tyt.auth.application.dto.result;

public record TokenResult(
	String accessToken,
	String refreshToken
) {
}
