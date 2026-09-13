package com.tyt.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tyt.common.exception.BusinessException;
import com.tyt.user.application.dto.result.UserResult;
import com.tyt.user.domain.exception.UserErrorCode;
import com.tyt.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

	private final UserRepository userRepository;

	public UserResult getById(Long userId) {
		return userRepository.findById(userId)
			.map(UserResult::from)
			.orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
	}
}
