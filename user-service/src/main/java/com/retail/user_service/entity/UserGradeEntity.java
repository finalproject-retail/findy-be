package com.retail.user_service.entity;

import com.retail.user_service.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_grades")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserGradeEntity extends BaseTimeEntity {
    @Id
    @Column(name = "grade_id")
    private Long gradeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade_name", nullable = false)
    private Grade gradeName;

    @Column(name = "criteria_amount", nullable = false)
    private long criteriaAmount; // 등급 달성 기준 금액

    @Column(name = "reward_rate", nullable = false)
    private double rewardRate; // 적립률
}
