package com.princesses7.findy.user.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.princesses7.findy.user.user.entity.Grade;
import com.princesses7.findy.user.user.entity.UserGradeEntity;

@Repository
public interface UserGradeRepository extends JpaRepository<UserGradeEntity, Long> {

	Optional<UserGradeEntity> findByGradeName(Grade gradeName);

	Optional<UserGradeEntity> findTopByCriteriaAmountLessThanEqualOrderByCriteriaAmountDesc(long criteriaAmount);
}