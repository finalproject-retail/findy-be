package com.princesses7.findy.user.reward.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

@Component
public class RewardCalculator {

	private static final BigDecimal PERCENT_DIVISOR = BigDecimal.valueOf(100);

	public long calculate(long paymentAmount, double rewardRate) {
		if (paymentAmount <= 0 || rewardRate <= 0) {
			return 0;
		}

		return BigDecimal.valueOf(paymentAmount)
			.multiply(BigDecimal.valueOf(rewardRate))
			.divide(PERCENT_DIVISOR, 0, RoundingMode.DOWN)
			.longValue();
	}
}