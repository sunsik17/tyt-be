package com.tyt.auth.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthCommandService {

	private final SocialAuthPort socialAuthPort;
	private final SocialAccountRepository socialAccountRepository;
	private final UserRegistrationPort userRegistrationPort;
	private final UserDeletionPort userDeletionPort;
	private final AuthTokenPort authTokenPort;
	private final RefreshTokenPort refreshTokenPort;

	@Transactional
	public TokenResult loginWithKakao(KakaoLoginCommand command) {
		String kakaoId = socialAuthPort.getKakaoId(command.accessToken());

		Long userId = socialAccountRepository.findByProviderAndSocialId(SocialProvider.KAKAO, kakaoId)
			.map(SocialAccount::getUserId)
			.orElseGet(() -> registerKakaoAccount(kakaoId));

		return issueTokens(userId);
	}

	public TokenResult reissue(ReissueTokenCommand command) {
		Long userId = authTokenPort.parseRefreshToken(command.refreshToken())
			.orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN));

		// 꺼내면서 지우므로, 서명은 유효하지만 이미 교체된 토큰이 들어오면 현재 토큰까지 무효가 되어 다시 로그인해야 한다
		String storedToken = refreshTokenPort.consume(userId)
			.orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN));
		if (!storedToken.equals(command.refreshToken())) {
			throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}

		return issueTokens(userId);
	}

	/**
	 * access 토큰은 서명만 확인하므로 만료(최대 30분)까지 유효하다. 재발급을 막기 위해 refresh 토큰을 지운다.
	 */
	public void logout(Long userId) {
		refreshTokenPort.delete(userId);
	}

	/**
	 * 카카오 연결을 먼저 끊고 데이터를 지운다. 연결 끊기가 실패하면 아무것도 지우지 않아 다시 시도할 수 있다.
	 * 연결은 끊겼는데 삭제가 실패해도, 다시 시도하면 이미 끊긴 연결은 성공으로 보고 삭제를 이어간다.
	 */
	@Transactional
	public void withdraw(Long userId) {
		List<SocialAccount> socialAccounts = socialAccountRepository.findAllByUserId(userId);
		socialAccounts.forEach(socialAccount -> socialAuthPort.unlinkKakao(socialAccount.getSocialId()));

		socialAccountRepository.deleteAll(socialAccounts);
		userDeletionPort.delete(userId);
		refreshTokenPort.delete(userId);
	}

	private Long registerKakaoAccount(String kakaoId) {
		Long userId = userRegistrationPort.register();
		socialAccountRepository.save(SocialAccount.create(SocialProvider.KAKAO, kakaoId, userId));

		return userId;
	}

	private TokenResult issueTokens(Long userId) {
		TokenResult tokens = authTokenPort.issue(userId);
		refreshTokenPort.save(userId, tokens.refreshToken());

		return tokens;
	}
}
