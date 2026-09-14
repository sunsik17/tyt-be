package com.tyt.common.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;

import com.tyt.common.exception.ErrorType;

class HttpStatusResolverTest {

	@DisplayName("ErrorType은 정해진 HttpStatus로 변환된다")
	@ParameterizedTest
	@CsvSource({
		"INVALID, BAD_REQUEST",
		"UNAUTHORIZED, UNAUTHORIZED",
		"NOT_FOUND, NOT_FOUND",
		"CONFLICT, CONFLICT",
		"FORBIDDEN, FORBIDDEN",
		"INTERNAL, INTERNAL_SERVER_ERROR"
	})
	void resolve(ErrorType errorType, HttpStatus expected) {
		assertThat(HttpStatusResolver.resolve(errorType)).isEqualTo(expected);
	}
}
