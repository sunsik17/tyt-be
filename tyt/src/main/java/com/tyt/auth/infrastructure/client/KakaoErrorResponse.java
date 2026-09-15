package com.tyt.auth.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 카카오 API 에러 응답. 예: {"msg":"NotRegisteredUserException","code":-101}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoErrorResponse(
	String msg,
	Integer code
) {
}
