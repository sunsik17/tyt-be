package com.tyt.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * TytApplication에 두면 @WebMvcTest가 jpaAuditingHandler를 만들려다
 * JPA 메타모델이 없어 실패한다. 그래서 별도 설정으로 분리한다.
 * JPA 슬라이스 테스트에서는 @Import로 직접 넣는다.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
