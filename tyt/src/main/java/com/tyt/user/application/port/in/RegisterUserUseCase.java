package com.tyt.user.application.port.in;

import com.tyt.user.application.dto.result.UserResult;

/**
 * 사용자를 가입시킨다. 어떤 인증 수단으로 가입하는지는 알지 않는다.
 * auth 도메인이 처음 보는 소셜 계정을 만났을 때 호출한다.
 */
public interface RegisterUserUseCase {

	UserResult register();
}
