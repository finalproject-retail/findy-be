package com.retail.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.retail.user_service.entity.Grade;
import com.retail.user_service.entity.UserGradeEntity;

@Repository
public interface UserGradeRepository extends JpaRepository<UserGradeEntity, Long> {

    Optional<UserGradeEntity> findByGradeName(Grade gradeName);
}
