package com.tyt.user.presentation.dto.response;

import java.time.LocalDateTime;

import com.tyt.user.application.dto.result.UserResult;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserResponse(
	@Schema(description = "사용자 id", example = "1")
	Long id,

	@Schema(description = "가입 시각. 오프셋 없는 ISO-8601이며 서버 시간대 기준이다", example = "2026-09-14T00:00:00")
	LocalDateTime createdAt
) {

	public static UserResponse from(UserResult result) {
		return new UserResponse(result.id(), result.createdAt());
	}
}
