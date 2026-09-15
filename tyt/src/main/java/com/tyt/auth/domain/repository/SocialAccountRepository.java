package com.tyt.auth.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tyt.auth.domain.constants.SocialProvider;
import com.tyt.auth.domain.model.SocialAccount;

public interface SocialAccountRepository {

	Optional<SocialAccount> findByProviderAndSocialId(SocialProvider provider, String socialId);

	List<SocialAccount> findAllByUserId(Long userId);

	SocialAccount save(SocialAccount socialAccount);

	void deleteAll(List<SocialAccount> socialAccounts);
}
