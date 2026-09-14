package com.tyt.auth.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tyt.auth.application.dto.command.KakaoLoginCommand;
import com.tyt.auth.application.dto.command.ReissueTokenCommand;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthCommandService {

	private final SocialAuthPort socialAuthPort;
	private final SocialAccountRepository socialAccountRepository;
	private final UserRegistrationPort userRegistrationPort;
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
