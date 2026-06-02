package com.princesses7.findy.shopping.recentview.entity;

import java.time.LocalDateTime;

import com.princesses7.findy.shopping.global.entity.BaseTimeEntity;
import com.princesses7.findy.shopping.product.entity.Product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(name = "viewed_at", nullable = false)
	private LocalDateTime viewedAt;

	private RecentViewProduct(Long userId, Product product) {
		this.userId = userId;
		this.product = product;
		this.viewedAt = LocalDateTime.now();
	}

	public static RecentViewProduct create(Long userId, Product product) {
		return new RecentViewProduct(userId, product);
	}

	public void refreshViewedAt() {
		this.viewedAt = LocalDateTime.now();
	}
}