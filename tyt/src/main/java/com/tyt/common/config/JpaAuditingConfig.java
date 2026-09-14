package com.tyt.common.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * TytApplication에 두면 @WebMvcTest가 jpaAuditingHandler를 만들려다
 * JPA 메타모델이 없어 실패한다. 그래서 별도 설정으로 분리한다.
 * JPA 슬라이스 테스트에서는 @Import로 직접 넣는다.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

	/**
	 * 인증된 요청이면 인증 주체인 userId를 생성자·수정자로 쓴다.
	 * 로그인 중 가입처럼 인증 전에 저장하면 비어 있다.
	 */
	@Bean
	public AuditorAware<Long> auditorAware() {
		return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
			.map(Authentication::getPrincipal)
			.filter(Long.class::isInstance)
			.map(Long.class::cast);
	}
}
