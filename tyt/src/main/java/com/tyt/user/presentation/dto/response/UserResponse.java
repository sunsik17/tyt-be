package com.tyt.user.presentation.dto.response;

import java.time.LocalDateTime;

import com.tyt.user.application.dto.result.UserResult;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserResponse(
	@Schema(description = "사용자 id", example = "1")
	Long id,

	@Schema(description = "가입 시각. 한국 시간(Asia/Seoul) 기준이며 오프셋 없는 ISO-8601이다", example = "2026-09-14T00:00:00")
	LocalDateTime createdAt
) {

	public static UserResponse from(UserResult result) {
		return new UserResponse(result.id(), result.createdAt());
	}
}
