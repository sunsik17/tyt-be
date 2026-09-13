package com.tyt.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tyt.user.domain.model.User;

interface UserJpaRepository extends JpaRepository<User, Long> {
}
