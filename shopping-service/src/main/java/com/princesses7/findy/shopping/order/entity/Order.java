package com.princesses7.findy.shopping.order.entity;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.ArrayList;
import java.util.List;

import com.princesses7.findy.shopping.global.entity.BaseTimeEntity;
import com.princesses7.findy.shopping.order.exception.OrderException;

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
		Long couponId,
		int totalAmount,
		int discountAmount,
		int finalAmount
	) {
		Order order = new Order();
		order.userId = userId;
		order.shoppingListId = shoppingListId;
		order.couponId = couponId;
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

	// TODO: 취소된 주문은 완료 처리 불가 정책까지 추가 시 확장
	public void complete() {
		this.orderStatus = OrderStatus.COMPLETED;
	}

	public boolean isOwnedBy(Long userId) {
		return this.userId.equals(userId);
	}

	public void validateOwner(Long userId) {
		if (!isOwnedBy(userId)) {
			throw new OrderException(ORDER_ACCESS_DENIED);
		}
	}
}