package com.tyt.common.web;

import com.tyt.common.exception.ErrorCode;

public record ApiResponse<T>(
	String code,
	String message,
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
