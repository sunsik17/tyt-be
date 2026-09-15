package com.tyt.common.web;

import com.tyt.common.exception.ErrorCode;

import io.swagger.v3.oas.annotations.media.Schema;

public record ApiResponse<T>(
	@Schema(description = "성공·실패 코드. 화면 분기는 이 값으로 한다", example = "LOGIN_SUCCESS")
	String code,

	@Schema(description = "사람이 읽는 메시지. 문구가 바뀔 수 있으니 분기에 쓰지 않는다", example = "로그인했습니다.")
	String message,

	@Schema(description = "성공 시 응답 본문. 실패 시 null")
	T data
) {

	public static <T> ApiResponse<T> success(SuccessCode successCode, T data) {
		return new ApiResponse<>(successCode.name(), successCode.getMessage(), data);
	}

	public static ApiResponse<Void> success(SuccessCode successCode) {
		return new ApiResponse<>(successCode.name(), successCode.getMessage(), null);
	}

	public static ApiResponse<Void> error(ErrorCode errorCode) {
		return new ApiResponse<>(errorCode.name(), errorCode.getMessage(), null);
	}

	public static ApiResponse<Void> error(ErrorCode errorCode, String message) {
		return new ApiResponse<>(errorCode.name(), message, null);
	}
}
