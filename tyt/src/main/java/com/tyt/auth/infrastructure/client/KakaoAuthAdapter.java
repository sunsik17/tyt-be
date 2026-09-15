package com.tyt.auth.infrastructure.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.tyt.auth.application.port.out.SocialAuthPort;
import com.tyt.auth.domain.exception.AuthErrorCode;
import com.tyt.auth.infrastructure.config.KakaoProperties;
import com.tyt.common.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoAuthAdapter implements SocialAuthPort {

	/**
	 * 해당 앱에 카카오계정 연결이 완료되지 않은 사용자. 연결 끊기에서는 이미 끊긴 것으로 본다.
	 */
	private static final int NOT_LINKED_USER_CODE = -101;

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

	@Override
	public void unlinkKakao(String kakaoId) {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("target_id_type", "user_id");
		form.add("target_id", kakaoId);

		try {
			kakaoRestClient.post()
				.uri("/v1/user/unlink")
				.header(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoProperties.adminKey())
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(form)
				.retrieve()
				.toBodilessEntity();
		} catch (HttpClientErrorException e) {
			// 탈퇴를 다시 시도할 때 이미 끊긴 연결 때문에 막히지 않도록 성공으로 본다
			if (isNotLinkedUser(e)) {
				return;
			}
			log.warn("카카오 연결 끊기 실패. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
			throw new BusinessException(AuthErrorCode.SOCIAL_UNLINK_FAILED);
		} catch (RestClientException e) {
			log.warn("카카오 연결 끊기 실패", e);
			throw new BusinessException(AuthErrorCode.SOCIAL_UNLINK_FAILED);
		}
	}

	private boolean isNotLinkedUser(HttpClientErrorException e) {
		try {
			KakaoErrorResponse error = e.getResponseBodyAs(KakaoErrorResponse.class);
			return error != null && error.code() != null && error.code() == NOT_LINKED_USER_CODE;
		} catch (RuntimeException parseFailure) {
			return false;
		}
	}
}
