package com.tyt.auth.application.port.out;

public interface UserDeletionPort {

	/**
	 * 사용자를 삭제한다. 이미 없으면 아무것도 하지 않는다.
	 */
	void delete(Long userId);
}
