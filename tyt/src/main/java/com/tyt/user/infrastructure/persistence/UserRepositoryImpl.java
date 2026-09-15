package com.tyt.user.infrastructure.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tyt.user.domain.model.User;
import com.tyt.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

	private final UserJpaRepository userJpaRepository;

	@Override
	public Optional<User> findById(Long id) {
		return userJpaRepository.findById(id);
	}

	@Override
	public User save(User user) {
		return userJpaRepository.save(user);
	}

	@Override
	public void deleteById(Long id) {
		userJpaRepository.deleteById(id);
	}
}
