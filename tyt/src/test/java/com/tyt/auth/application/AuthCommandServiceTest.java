package com.tyt.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tyt.auth.application.dto.command.KakaoLoginCommand;
import com.tyt.auth.application.dto.command.ReissueTokenCommand;
import com.tyt.auth.application.dto.result.TokenResult;
import com.tyt.auth.application.port.out.AuthTokenPort;
import com.tyt.auth.application.port.out.RefreshTokenPort;
import com.tyt.auth.application.port.out.SocialAuthPort;
import com.tyt.auth.application.port.out.UserDeletionPort;
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
	private UserDeletionPort userDeletionPort;

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

	@DisplayName("저장된 refresh 토큰과 같으면 새 토큰을 발급하고 새 refresh를 저장한다")
	@Test
	void reissue() {
		given(authTokenPort.parseRefreshToken("old-refresh")).willReturn(Optional.of(1L));
		given(refreshTokenPort.consume(1L)).willReturn(Optional.of("old-refresh"));
		given(authTokenPort.issue(1L)).willReturn(tokens);

		TokenResult result = authCommandService.reissue(new ReissueTokenCommand("old-refresh"));

		assertThat(result).isEqualTo(tokens);
		verify(refreshTokenPort).save(1L, "refresh");
	}

	@DisplayName("refresh 토큰이 유효하지 않으면 저장소를 건드리지 않고 INVALID_REFRESH_TOKEN")
	@Test
	void reissueWithInvalidToken() {
		given(authTokenPort.parseRefreshToken("invalid")).willReturn(Optional.empty());

		assertInvalidRefreshToken("invalid");

		verify(refreshTokenPort, never()).consume(any());
	}

	@DisplayName("저장된 refresh 토큰이 없으면 INVALID_REFRESH_TOKEN")
	@Test
	void reissueWithoutStoredToken() {
		given(authTokenPort.parseRefreshToken("old-refresh")).willReturn(Optional.of(1L));
		given(refreshTokenPort.consume(1L)).willReturn(Optional.empty());

		assertInvalidRefreshToken("old-refresh");
	}

	@DisplayName("이미 교체된 refresh 토큰이면 INVALID_REFRESH_TOKEN")
	@Test
	void reissueWithRotatedToken() {
		given(authTokenPort.parseRefreshToken("rotated-refresh")).willReturn(Optional.of(1L));
		given(refreshTokenPort.consume(1L)).willReturn(Optional.of("current-refresh"));

		assertInvalidRefreshToken("rotated-refresh");
	}

	private void assertInvalidRefreshToken(String refreshToken) {
		assertThatThrownBy(() -> authCommandService.reissue(new ReissueTokenCommand(refreshToken)))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException)e).getErrorCode())
			.isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);

		verify(authTokenPort, never()).issue(any());
		verify(refreshTokenPort, never()).save(any(), any());
	}

	@DisplayName("로그아웃하면 저장된 refresh 토큰을 지운다")
	@Test
	void logout() {
		authCommandService.logout(1L);

		verify(refreshTokenPort).delete(1L);
	}

	@DisplayName("탈퇴하면 카카오 연결을 끊고 소셜 계정·사용자·refresh 토큰을 지운다")
	@Test
	void withdraw() {
		List<SocialAccount> socialAccounts = List.of(SocialAccount.create(SocialProvider.KAKAO, "12345", 1L));
		given(socialAccountRepository.findAllByUserId(1L)).willReturn(socialAccounts);

		authCommandService.withdraw(1L);

		verify(socialAuthPort).unlinkKakao("12345");
		verify(socialAccountRepository).deleteAll(socialAccounts);
		verify(userDeletionPort).delete(1L);
		verify(refreshTokenPort).delete(1L);
	}

	@DisplayName("카카오 연결 끊기에 실패하면 아무것도 지우지 않는다")
	@Test
	void withdrawWhenUnlinkFails() {
		given(socialAccountRepository.findAllByUserId(1L))
			.willReturn(List.of(SocialAccount.create(SocialProvider.KAKAO, "12345", 1L)));
		willThrow(new BusinessException(AuthErrorCode.SOCIAL_UNLINK_FAILED)).given(socialAuthPort).unlinkKakao("12345");

		assertThatThrownBy(() -> authCommandService.withdraw(1L))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException)e).getErrorCode())
			.isEqualTo(AuthErrorCode.SOCIAL_UNLINK_FAILED);

		verify(socialAccountRepository, never()).deleteAll(any());
		verify(userDeletionPort, never()).delete(any());
		verify(refreshTokenPort, never()).delete(any());
	}
}
