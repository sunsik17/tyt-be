package com.tyt.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tyt.user.application.dto.result.UserResult;
import com.tyt.user.application.port.in.RegisterUserUseCase;
import com.tyt.user.domain.model.User;
import com.tyt.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserCommandService implements RegisterUserUseCase {

	private final UserRepository userRepository;

	@Override
	@Transactional
	public UserResult register() {
		return UserResult.from(userRepository.save(User.create()));
	}
}
