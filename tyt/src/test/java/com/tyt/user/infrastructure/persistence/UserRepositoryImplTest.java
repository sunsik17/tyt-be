package com.tyt.user.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.tyt.common.config.JpaAuditingConfig;
import com.tyt.user.domain.model.User;

@DataJpaTest
@Import({UserRepositoryImpl.class, JpaAuditingConfig.class})
class UserRepositoryImplTest {

	@Autowired
	private UserRepositoryImpl userRepositoryImpl;

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

	@DisplayName("인증이 없어 생성자·수정자는 비어 있다")
	@Test
	void actorFieldsAreEmptyWithoutAuthentication() {
		User saved = userRepositoryImpl.save(User.create());

		assertThat(saved.getCreatedBy()).isNull();
		assertThat(saved.getUpdatedBy()).isNull();
	}
}
