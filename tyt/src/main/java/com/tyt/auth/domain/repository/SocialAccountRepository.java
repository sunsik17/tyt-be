package com.tyt.auth.domain.repository;

import java.util.Optional;

import com.tyt.auth.domain.constants.SocialProvider;
import com.tyt.auth.domain.model.SocialAccount;

public interface SocialAccountRepository {

	Optional<SocialAccount> findByProviderAndSocialId(SocialProvider provider, String socialId);

	SocialAccount save(SocialAccount socialAccount);
}
