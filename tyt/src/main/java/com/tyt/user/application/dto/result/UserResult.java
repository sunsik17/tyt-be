package com.tyt.user.application.dto.result;

import java.time.LocalDateTime;

import com.tyt.user.domain.model.User;

public record UserResult(
	Long id,
	LocalDateTime createdAt
) {

	public static UserResult from(User user) {
		return new UserResult(user.getId(), user.getCreatedAt());
	}
}
