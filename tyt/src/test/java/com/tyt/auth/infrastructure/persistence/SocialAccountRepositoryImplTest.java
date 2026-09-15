package com.tyt.auth.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.tyt.auth.domain.constants.SocialProvider;
import com.tyt.auth.domain.model.SocialAccount;

@DataJpaTest
@Import(SocialAccountRepositoryImpl.class)
class SocialAccountRepositoryImplTest {

	@Autowired
	private SocialAccountRepositoryImpl socialAccountRepositoryImpl;

	@DisplayName("저장한 소셜 계정을 provider와 소셜 id로 찾는다")
	@Test
	void saveAndFind() {
		socialAccountRepositoryImpl.save(SocialAccount.create(SocialProvider.KAKAO, "12345", 1L));

		Optional<SocialAccount> found =
			socialAccountRepositoryImpl.findByProviderAndSocialId(SocialProvider.KAKAO, "12345");

		assertThat(found).isPresent();
		assertThat(found.get().getUserId()).isEqualTo(1L);
	}

	@DisplayName("연결되지 않은 소셜 계정은 찾지 못한다")
	@Test
	void findNothing() {
		assertThat(socialAccountRepositoryImpl.findByProviderAndSocialId(SocialProvider.KAKAO, "없는id")).isEmpty();
	}

	@DisplayName("사용자의 소셜 계정만 모두 찾아 지운다")
	@Test
	void findAllByUserIdAndDeleteAll() {
		socialAccountRepositoryImpl.save(SocialAccount.create(SocialProvider.KAKAO, "12345", 1L));
		socialAccountRepositoryImpl.save(SocialAccount.create(SocialProvider.KAKAO, "67890", 2L));

		List<SocialAccount> socialAccounts = socialAccountRepositoryImpl.findAllByUserId(1L);
		assertThat(socialAccounts).extracting(SocialAccount::getSocialId).containsExactly("12345");

		socialAccountRepositoryImpl.deleteAll(socialAccounts);

		assertThat(socialAccountRepositoryImpl.findAllByUserId(1L)).isEmpty();
		assertThat(socialAccountRepositoryImpl.findAllByUserId(2L)).hasSize(1);
	}
}
