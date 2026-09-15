package com.tyt.auth.presentation.dto.response.constants;

import com.tyt.common.web.SuccessCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthSuccessCode implements SuccessCode {

	LOGIN_SUCCESS("로그인했습니다."),
	TOKEN_REISSUED("토큰을 재발급했습니다."),
	LOGGED_OUT("로그아웃했습니다."),
	ACCOUNT_DELETED("탈퇴했습니다.");

	private final String message;
}
