package com.princesses7.findy.shopping.order.entity;

import com.princesses7.findy.shopping.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "order_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "order_item_id")
	private Long orderItemId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@Column(name = "cart_item_id")
	private Long cartItemId;

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@Column(name = "quantity", nullable = false)
	private int quantity;

	@Column(name = "product_price", nullable = false)
	private int productPrice;

	@Column(name = "discount_amount", nullable = false)
	private int discountAmount;

	@Column(name = "final_amount", nullable = false)
	private int finalAmount;

	public static OrderItem create(
		Long productId,
		int quantity,
		int productPrice,
		int discountAmount,
		int finalAmount
	) {
		OrderItem orderItem = new OrderItem();
		orderItem.productId = productId;
		orderItem.quantity = quantity;
		orderItem.productPrice = productPrice;
		orderItem.discountAmount = discountAmount;
		orderItem.finalAmount = finalAmount;
		return orderItem;
	}

	void assignOrder(Order order) {
		this.order = order;
	}
}