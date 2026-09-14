package com.tyt.auth.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tyt.auth.application.dto.command.KakaoLoginCommand;
import com.tyt.auth.application.dto.result.TokenResult;
import com.tyt.auth.application.port.out.AuthTokenPort;
import com.tyt.auth.application.port.out.RefreshTokenPort;
import com.tyt.auth.application.port.out.SocialAuthPort;
import com.tyt.auth.application.port.out.UserRegistrationPort;
import com.tyt.auth.domain.constants.SocialProvider;
import com.tyt.auth.domain.model.SocialAccount;
import com.tyt.auth.domain.repository.SocialAccountRepository;

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

		TokenResult tokens = authTokenPort.issue(userId);
		refreshTokenPort.save(userId, tokens.refreshToken());

		return tokens;
	}

	private Long registerKakaoAccount(String kakaoId) {
		Long userId = userRegistrationPort.register();
		socialAccountRepository.save(SocialAccount.create(SocialProvider.KAKAO, kakaoId, userId));

		return userId;
	}
}
