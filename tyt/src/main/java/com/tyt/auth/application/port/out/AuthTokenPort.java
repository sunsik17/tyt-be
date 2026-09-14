package com.tyt.auth.application.port.out;

import com.tyt.auth.application.dto.result.TokenResult;

public interface AuthTokenPort {

	TokenResult issue(Long userId);
}
