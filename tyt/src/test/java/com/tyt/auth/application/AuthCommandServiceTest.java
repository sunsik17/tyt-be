package com.tyt.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tyt.auth.application.dto.command.KakaoLoginCommand;
import com.tyt.auth.application.dto.result.TokenResult;
import com.tyt.auth.application.port.out.AuthTokenPort;
import com.tyt.auth.application.port.out.RefreshTokenPort;
import com.tyt.auth.application.port.out.SocialAuthPort;
import com.tyt.auth.application.port.out.UserRegistrationPort;
import com.tyt.auth.domain.constants.SocialProvider;
import com.tyt.auth.domain.exception.AuthErrorCode;
import com.tyt.auth.domain.model.SocialAccount;
import com.tyt.auth.domain.repository.SocialAccountRepository;
import com.tyt.common.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class AuthCommandServiceTest {

	@Mock
	private SocialAuthPort socialAuthPort;

	@Mock
	private SocialAccountRepository socialAccountRepository;

	@Mock
	private UserRegistrationPort userRegistrationPort;

	@Mock
	private AuthTokenPort authTokenPort;

	@Mock
	private RefreshTokenPort refreshTokenPort;

	@InjectMocks
	private AuthCommandService authCommandService;

	private final KakaoLoginCommand command = new KakaoLoginCommand("kakao-access-token");
	private final TokenResult tokens = new TokenResult("access", "refresh");

	@DisplayName("이미 연결된 카카오 계정이면 가입 없이 토큰을 발급하고 refresh를 저장한다")
	@Test
	void loginWithLinkedAccount() {
		given(socialAuthPort.getKakaoId("kakao-access-token")).willReturn("12345");
		given(socialAccountRepository.findByProviderAndSocialId(SocialProvider.KAKAO, "12345"))
			.willReturn(Optional.of(SocialAccount.create(SocialProvider.KAKAO, "12345", 1L)));
		given(authTokenPort.issue(1L)).willReturn(tokens);

		TokenResult result = authCommandService.loginWithKakao(command);

		assertThat(result).isEqualTo(tokens);
		verify(userRegistrationPort, never()).register();
		verify(socialAccountRepository, never()).save(any());
		verify(refreshTokenPort).save(1L, "refresh");
	}

	@DisplayName("처음 보는 카카오 계정이면 사용자를 가입시키고 계정을 연결한다")
	@Test
	void loginWithNewAccount() {
		given(socialAuthPort.getKakaoId("kakao-access-token")).willReturn("12345");
		given(socialAccountRepository.findByProviderAndSocialId(SocialProvider.KAKAO, "12345"))
			.willReturn(Optional.empty());
		given(userRegistrationPort.register()).willReturn(2L);
		given(authTokenPort.issue(2L)).willReturn(tokens);

		authCommandService.loginWithKakao(command);

		ArgumentCaptor<SocialAccount> captor = ArgumentCaptor.forClass(SocialAccount.class);
		verify(socialAccountRepository).save(captor.capture());
		assertThat(captor.getValue().getProvider()).isEqualTo(SocialProvider.KAKAO);
		assertThat(captor.getValue().getSocialId()).isEqualTo("12345");
		assertThat(captor.getValue().getUserId()).isEqualTo(2L);
		verify(refreshTokenPort).save(2L, "refresh");
	}

	@DisplayName("카카오 토큰이 유효하지 않으면 가입도 토큰 발급도 하지 않는다")
	@Test
	void loginWithInvalidKakaoToken() {
		given(socialAuthPort.getKakaoId("kakao-access-token"))
			.willThrow(new BusinessException(AuthErrorCode.INVALID_SOCIAL_TOKEN));

		assertThatThrownBy(() -> authCommandService.loginWithKakao(command))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException)e).getErrorCode())
			.isEqualTo(AuthErrorCode.INVALID_SOCIAL_TOKEN);

		verify(userRegistrationPort, never()).register();
		verify(authTokenPort, never()).issue(any());
		verify(refreshTokenPort, never()).save(any(), any());
	}
}
