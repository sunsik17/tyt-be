package com.tyt.user.domain.model;

import com.tyt.common.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자. 어떤 수단으로 로그인했는지는 알지 않는다.
 * 소셜 계정과 사용자의 연결은 auth 도메인이 가진다.
 *
 * 테이블 이름이 users인 것은 user가 여러 DB에서 예약어이기 때문이다.
 */
@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	public static User create() {
		return new User();
	}
}
