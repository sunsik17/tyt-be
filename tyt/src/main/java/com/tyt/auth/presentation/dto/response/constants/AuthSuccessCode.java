package com.tyt.auth.presentation.dto.response.constants;

import com.tyt.common.web.SuccessCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthSuccessCode implements SuccessCode {

	LOGIN_SUCCESS("로그인했습니다.");

	private final String message;
}
