package com.princesses7.findy.shopping.order.entity;

import java.util.ArrayList;
import java.util.List;

import com.princesses7.findy.shopping.global.entity.BaseTimeEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "order_id")
	private Long orderId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "shopping_list_id")
	private Long shoppingListId;

	@Column(name = "coupon_id")
	private Long couponId;

	@Column(name = "total_amount", nullable = false)
	private int totalAmount;

	@Column(name = "discount_amount", nullable = false)
	private int discountAmount;

	@Column(name = "final_amount", nullable = false)
	private int finalAmount;

	@Column(name = "earned_reward", nullable = false)
	private int earnedReward;

	@Enumerated(EnumType.STRING)
	@Column(name = "order_status", nullable = false, length = 30)
	private OrderStatus orderStatus;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> orderItems = new ArrayList<>();

	public static Order create(
		Long userId,
		Long shoppingListId,
		int totalAmount,
		int discountAmount,
		int finalAmount
	) {
		Order order = new Order();
		order.userId = userId;
		order.shoppingListId = shoppingListId;
		order.couponId = null;
		order.totalAmount = totalAmount;
		order.discountAmount = discountAmount;
		order.finalAmount = finalAmount;
		order.earnedReward = 0;
		order.orderStatus = OrderStatus.CREATED;
		return order;
	}

	public void addOrderItem(OrderItem orderItem) {
		orderItems.add(orderItem);
		orderItem.assignOrder(this);
	}
}