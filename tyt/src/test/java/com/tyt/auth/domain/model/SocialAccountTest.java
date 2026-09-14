package com.tyt.auth.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.tyt.auth.domain.constants.SocialProvider;
import com.tyt.auth.domain.exception.AuthErrorCode;
import com.tyt.common.exception.BusinessException;

class SocialAccountTest {

	@DisplayName("소셜 계정을 사용자에 연결한다")
	@Test
	void create() {
		SocialAccount account = SocialAccount.create(SocialProvider.KAKAO, "12345", 1L);

		assertThat(account.getProvider()).isEqualTo(SocialProvider.KAKAO);
		assertThat(account.getSocialId()).isEqualTo("12345");
		assertThat(account.getUserId()).isEqualTo(1L);
	}

	@DisplayName("소셜 id가 비어 있으면 INVALID_SOCIAL_ACCOUNT")
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" "})
	void createWithBlankSocialId(String socialId) {
		assertThatThrownBy(() -> SocialAccount.create(SocialProvider.KAKAO, socialId, 1L))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException)e).getErrorCode())
			.isEqualTo(AuthErrorCode.INVALID_SOCIAL_ACCOUNT);
	}

	@DisplayName("provider가 없으면 INVALID_SOCIAL_ACCOUNT")
	@Test
	void createWithoutProvider() {
		assertThatThrownBy(() -> SocialAccount.create(null, "12345", 1L))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException)e).getErrorCode())
			.isEqualTo(AuthErrorCode.INVALID_SOCIAL_ACCOUNT);
	}

	@DisplayName("연결할 사용자가 없으면 INVALID_SOCIAL_ACCOUNT")
	@Test
	void createWithoutUser() {
		assertThatThrownBy(() -> SocialAccount.create(SocialProvider.KAKAO, "12345", null))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException)e).getErrorCode())
			.isEqualTo(AuthErrorCode.INVALID_SOCIAL_ACCOUNT);
	}
}
