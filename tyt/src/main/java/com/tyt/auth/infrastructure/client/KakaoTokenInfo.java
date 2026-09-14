package com.tyt.auth.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * GET /v1/user/access_token_info 응답. id는 카카오 회원번호, app_id는 토큰이 발급된 앱.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoTokenInfo(
	Long id,
	@JsonProperty("app_id") Long appId
) {
}
