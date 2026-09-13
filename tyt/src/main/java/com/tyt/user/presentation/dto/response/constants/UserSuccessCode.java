package com.tyt.user.presentation.dto.response.constants;

import com.tyt.common.web.SuccessCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserSuccessCode implements SuccessCode {

	USER_FOUND("사용자를 조회했습니다.");

	private final String message;
}
