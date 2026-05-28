package com.princesses7.findy.recommendation.recommendation.log.repository.projection;

public interface PopularProductProjection {

	Long getProductId();

	Long getImpressionCount();

	Long getClickCount();

	Long getPopularityScore();
}