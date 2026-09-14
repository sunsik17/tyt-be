package com.tyt.auth.domain.model;

import com.tyt.auth.domain.constants.SocialProvider;
import com.tyt.auth.domain.exception.AuthErrorCode;
import com.tyt.common.entity.BaseEntity;
import com.tyt.common.exception.BusinessException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소셜 계정과 사용자의 연결. user 도메인은 이 연결을 모른다.
 * 사용자는 user 도메인 엔티티를 참조하지 않고 id로만 가진다.
 */
@Getter
@Entity
@Table(
	name = "social_accounts",
	uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "social_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccount extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false)
	private SocialProvider provider;

	@Column(name = "social_id", nullable = false, updatable = false)
	private String socialId;

	@Column(name = "user_id", nullable = false, updatable = false)
	private Long userId;

	private SocialAccount(SocialProvider provider, String socialId, Long userId) {
		this.provider = provider;
		this.socialId = socialId;
		this.userId = userId;
	}

	public static SocialAccount create(SocialProvider provider, String socialId, Long userId) {
		if (provider == null || socialId == null || socialId.isBlank() || userId == null) {
			throw new BusinessException(AuthErrorCode.INVALID_SOCIAL_ACCOUNT);
		}
		return new SocialAccount(provider, socialId, userId);
	}
}
