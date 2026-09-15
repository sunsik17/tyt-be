package com.tyt.user.application.port.in;

/**
 * 사용자를 삭제한다. 이미 없으면 아무것도 하지 않는다.
 * auth 도메인이 탈퇴할 때 호출한다.
 */
public interface DeleteUserUseCase {

	void delete(Long userId);
}
