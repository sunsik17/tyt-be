package com.tyt.auth.infrastructure.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.tyt.auth.application.port.out.SocialAuthPort;
import com.tyt.auth.domain.exception.AuthErrorCode;
import com.tyt.auth.infrastructure.config.KakaoProperties;
import com.tyt.common.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KakaoAuthAdapter implements SocialAuthPort {

	private final RestClient kakaoRestClient;
	private final KakaoProperties kakaoProperties;

	@Override
	public String getKakaoId(String accessToken) {
		KakaoTokenInfo tokenInfo = kakaoRestClient.get()
			.uri("/v1/user/access_token_info")
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
			.retrieve()
			.onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
				throw new BusinessException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
			})
			.body(KakaoTokenInfo.class);

		// 다른 앱에서 발급된 카카오 토큰으로 로그인하는 것을 막는다
		if (tokenInfo == null || !kakaoProperties.appId().equals(tokenInfo.appId())) {
			throw new BusinessException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
		}

		return String.valueOf(tokenInfo.id());
	}
}
