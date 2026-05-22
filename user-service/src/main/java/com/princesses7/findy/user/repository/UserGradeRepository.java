package com.princesses7.findy.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.princesses7.findy.user.entity.Grade;
import com.princesses7.findy.user.entity.UserGradeEntity;

@Repository
public interface UserGradeRepository extends JpaRepository<UserGradeEntity, Long> {

	Optional<UserGradeEntity> findByGradeName(Grade gradeName);
}
