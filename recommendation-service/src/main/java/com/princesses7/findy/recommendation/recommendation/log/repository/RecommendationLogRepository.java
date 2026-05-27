package com.princesses7.findy.recommendation.recommendation.log.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.recommendation.recommendation.log.entity.RecommendationLog;

public interface RecommendationLogRepository extends JpaRepository<RecommendationLog, Long> {
}