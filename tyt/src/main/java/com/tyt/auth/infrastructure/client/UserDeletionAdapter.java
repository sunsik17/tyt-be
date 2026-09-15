package com.tyt.auth.infrastructure.client;

import org.springframework.stereotype.Component;

import com.tyt.auth.application.port.out.UserDeletionPort;
import com.tyt.user.application.port.in.DeleteUserUseCase;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserDeletionAdapter implements UserDeletionPort {

	private final DeleteUserUseCase deleteUserUseCase;

	@Override
	public void delete(Long userId) {
		deleteUserUseCase.delete(userId);
	}
}
