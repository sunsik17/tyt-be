package com.tyt.user.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.tyt.common.config.JpaAuditingConfig;
import com.tyt.user.domain.model.User;

@DataJpaTest
@Import({UserRepositoryImpl.class, JpaAuditingConfig.class})
class UserRepositoryImplTest {

	@Autowired
	private UserRepositoryImpl userRepositoryImpl;

	@AfterEach
	void clearAuthentication() {
		SecurityContextHolder.clearContext();
	}

	@DisplayName("저장하면 id가 부여된다")
	@Test
	void save() {
		User saved = userRepositoryImpl.save(User.create());

		assertThat(saved.getId()).isNotNull();
	}

	@DisplayName("저장하면 감사 필드가 채워진다")
	@Test
	void auditFieldsArePopulated() {
		User saved = userRepositoryImpl.save(User.create());

		assertThat(saved.getCreatedAt()).isNotNull();
		assertThat(saved.getUpdatedAt()).isNotNull();
	}

	@DisplayName("인증된 요청에서 저장하면 생성자·수정자가 userId로 채워진다")
	@Test
	void actorFieldsArePopulatedWhenAuthenticated() {
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(1L, null, List.of()));

		User saved = userRepositoryImpl.save(User.create());

		assertThat(saved.getCreatedBy()).isEqualTo(1L);
		assertThat(saved.getUpdatedBy()).isEqualTo(1L);
	}

	@DisplayName("인증 없이 저장하면 생성자·수정자는 비어 있다 (로그인 중 가입)")
	@Test
	void actorFieldsAreEmptyWithoutAuthentication() {
		User saved = userRepositoryImpl.save(User.create());

		assertThat(saved.getCreatedBy()).isNull();
		assertThat(saved.getUpdatedBy()).isNull();
	}

	@DisplayName("사용자를 삭제하면 더 이상 찾을 수 없다")
	@Test
	void deleteById() {
		User saved = userRepositoryImpl.save(User.create());

		userRepositoryImpl.deleteById(saved.getId());

		assertThat(userRepositoryImpl.findById(saved.getId())).isEmpty();
	}

	@DisplayName("없는 사용자를 삭제해도 예외가 나지 않는다 (탈퇴 재시도)")
	@Test
	void deleteMissingUser() {
		assertThatCode(() -> userRepositoryImpl.deleteById(999L)).doesNotThrowAnyException();
	}
}
