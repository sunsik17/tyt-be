package com.tyt.auth.presentation.dto.request;

import com.tyt.auth.application.dto.command.ReissueTokenCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ReissueTokenRequest(
	@Schema(description = "로그인이나 직전 재발급에서 받은 refreshToken", example = "eyJhbGciOiJIUzI1NiJ9.refresh")
	@NotBlank String refreshToken
) {

	public ReissueTokenCommand toCommand() {
		return new ReissueTokenCommand(refreshToken);
	}
}
