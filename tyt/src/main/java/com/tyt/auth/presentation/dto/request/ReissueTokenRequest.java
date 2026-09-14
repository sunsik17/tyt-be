package com.tyt.auth.presentation.dto.request;

import com.tyt.auth.application.dto.command.ReissueTokenCommand;

import jakarta.validation.constraints.NotBlank;

public record ReissueTokenRequest(
	@NotBlank String refreshToken
) {

	public ReissueTokenCommand toCommand() {
		return new ReissueTokenCommand(refreshToken);
	}
}
