package com.princesses7.findy.user.recentview.entity;

import java.time.LocalDateTime;

import com.princesses7.findy.user.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "recent_view_products",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_recent_view_products_user_product",
			columnNames = {"user_id", "product_id"}
		)
	},
	indexes = {
		@Index(name = "idx_recent_view_products_user_viewed_at", columnList = "user_id, viewed_at"),
		@Index(name = "idx_recent_view_products_product_id", columnList = "product_id")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecentViewProduct extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "recent_view_id")
	private Long recentViewId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@Column(name = "viewed_at", nullable = false)
	private LocalDateTime viewedAt;

	private RecentViewProduct(Long userId, Long productId) {
		this.userId = userId;
		this.productId = productId;
		this.viewedAt = LocalDateTime.now();
	}

	public static RecentViewProduct create(Long userId, Long productId) {
		return new RecentViewProduct(userId, productId);
	}

	public void refreshViewedAt() {
		this.viewedAt = LocalDateTime.now();
	}
}