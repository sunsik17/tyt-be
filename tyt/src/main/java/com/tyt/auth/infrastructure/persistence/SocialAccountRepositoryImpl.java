package com.tyt.auth.infrastructure.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tyt.auth.domain.constants.SocialProvider;
import com.tyt.auth.domain.model.SocialAccount;
import com.tyt.auth.domain.repository.SocialAccountRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SocialAccountRepositoryImpl implements SocialAccountRepository {

	private final SocialAccountJpaRepository socialAccountJpaRepository;

	@Override
	public Optional<SocialAccount> findByProviderAndSocialId(SocialProvider provider, String socialId) {
		return socialAccountJpaRepository.findByProviderAndSocialId(provider, socialId);
	}

	@Override
	public SocialAccount save(SocialAccount socialAccount) {
		return socialAccountJpaRepository.save(socialAccount);
	}
}
