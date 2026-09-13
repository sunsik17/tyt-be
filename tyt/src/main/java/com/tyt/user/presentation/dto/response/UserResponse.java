package com.tyt.user.presentation.dto.response;

import java.time.LocalDateTime;

import com.tyt.user.application.dto.result.UserResult;

public record UserResponse(
	Long id,
	LocalDateTime createdAt
) {

	public static UserResponse from(UserResult result) {
		return new UserResponse(result.id(), result.createdAt());
	}
}
