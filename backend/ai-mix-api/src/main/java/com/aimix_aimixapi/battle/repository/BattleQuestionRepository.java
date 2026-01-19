package com.aimix_aimixapi.battle.repository;

import com.aimix_aimixapi.battle.entity.BattleQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * 배틀 문제 Repository
 * 배틀 문제 엔티티에 대한 데이터베이스 접근을 담당
 */
public interface BattleQuestionRepository extends JpaRepository<BattleQuestion, UUID> {
}
