package com.tyt.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.tyt.common.exception.BusinessException;
import com.tyt.user.application.dto.result.UserResult;
import com.tyt.user.domain.exception.UserErrorCode;
import com.tyt.user.domain.model.User;
import com.tyt.user.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserQueryService userQueryService;

	@DisplayName("id로 사용자를 조회한다")
	@Test
	void getById() {
		User user = User.create();
		ReflectionTestUtils.setField(user, "id", 1L);
		given(userRepository.findById(1L)).willReturn(Optional.of(user));

		UserResult result = userQueryService.getById(1L);

		assertThat(result.id()).isEqualTo(1L);
	}

	@DisplayName("없는 사용자면 USER_NOT_FOUND")
	@Test
	void getByIdNotFound() {
		given(userRepository.findById(1L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> userQueryService.getById(1L))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException)e).getErrorCode())
			.isEqualTo(UserErrorCode.USER_NOT_FOUND);
	}
}
