package com.tyt.auth.application.port.out;

public interface SocialAuthPort {

	/**
	 * 카카오 액세스 토큰이 우리 앱에서 발급된 유효한 토큰인지 확인하고 카카오 회원번호를 돌려준다.
	 * 유효하지 않으면 BusinessException(INVALID_SOCIAL_TOKEN).
	 */
	String getKakaoId(String accessToken);
}
