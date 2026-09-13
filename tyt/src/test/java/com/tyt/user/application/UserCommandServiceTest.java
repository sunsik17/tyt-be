package com.tyt.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.tyt.user.application.dto.result.UserResult;
import com.tyt.user.domain.model.User;
import com.tyt.user.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserCommandService userCommandService;

	@DisplayName("사용자를 저장하고 id를 돌려준다")
	@Test
	void register() {
		User saved = User.create();
		ReflectionTestUtils.setField(saved, "id", 1L);
		given(userRepository.save(any(User.class))).willReturn(saved);

		UserResult result = userCommandService.register();

		assertThat(result.id()).isEqualTo(1L);
		verify(userRepository).save(any(User.class));
	}
}
