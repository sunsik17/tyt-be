package com.tyt.auth.infrastructure.client;

import org.springframework.stereotype.Component;

import com.tyt.auth.application.port.out.UserRegistrationPort;
import com.tyt.user.application.port.in.RegisterUserUseCase;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserRegistrationAdapter implements UserRegistrationPort {

	private final RegisterUserUseCase registerUserUseCase;

	@Override
	public Long register() {
		return registerUserUseCase.register().id();
	}
}
