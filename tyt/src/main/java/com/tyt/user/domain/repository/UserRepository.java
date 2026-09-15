package com.tyt.user.domain.repository;

import java.util.Optional;

import com.tyt.user.domain.model.User;

public interface UserRepository {

	Optional<User> findById(Long id);

	User save(User user);

	void deleteById(Long id);
}
