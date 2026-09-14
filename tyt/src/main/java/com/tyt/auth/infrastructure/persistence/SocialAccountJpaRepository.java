package com.tyt.auth.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tyt.auth.domain.constants.SocialProvider;
import com.tyt.auth.domain.model.SocialAccount;

interface SocialAccountJpaRepository extends JpaRepository<SocialAccount, Long> {

	Optional<SocialAccount> findByProviderAndSocialId(SocialProvider provider, String socialId);
}
