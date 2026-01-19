package com.aimix_aimixapi.knowledge.service;

import com.aimix_aimixapi.common.exception.domain.knowledge.contribution.ContributionTypeRequiredException;
import com.aimix_aimixapi.common.exception.domain.knowledge.contribution.ContributorRequiredException;
import com.aimix_aimixapi.common.exception.domain.knowledge.contribution.KnowledgeCardRequiredException;
import com.aimix_aimixapi.knowledge.message.KnowledgeMessage;
import com.aimix_aimixapi.knowledge.entity.CardContribution;
import com.aimix_aimixapi.knowledge.entity.ContributionType;
import com.aimix_aimixapi.knowledge.entity.KnowledgeCard;
import com.aimix_aimixapi.knowledge.repository.CardContributionRepository;
import com.aimix_aimixapi.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 카드 기여 이력 서비스
 * 개념 카드에 대한 사용자 기여 내역을 기록하고 관리합니다.
 * - 카드 생성, 수정, 오답 보고 등의 기여 이력을 추적
 * - 기여자별 기여 내역 조회 지원
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class CardContributionService {

    private final CardContributionRepository contributionRepository;

    /**
     * 기여 이력 기록
     * 개념 카드에 대한 사용자의 기여 내역을 데이터베이스에 저장합니다.
     * 
     * @param card        개념 카드 엔티티 (null 불가)
     * @param user        기여자 엔티티 (null 불가)
     * @param type        기여 타입 (CREATE, UPDATE, MISTAKE_REPORT 등)
     * @param description 기여 설명 (선택적, null 가능)
     * @throws KnowledgeCardRequiredException card가 null인 경우
     * @throws ContributorRequiredException user가 null인 경우
     * @throws ContributionTypeRequiredException type이 null인 경우
     * @apiNote 점검O
     * @since 2025-12-29
     */
    @Transactional
    public void recordContribution(KnowledgeCard card, User user, ContributionType type, String description) {
        log.info("기여 이력 기록 요청: cardId={}, userId={}, type={}, description={}",
                card != null ? card.getId() : null,
                user != null ? user.getId() : null,
                type,
                description);

        // null 체크
        if (card == null) {
            log.error("카드가 null입니다 - 기여 이력 기록 실패");
            throw new KnowledgeCardRequiredException(KnowledgeMessage.CARD_REQUIRED.getMessage());
        }

        if (user == null) {
            log.error("사용자가 null입니다 - 기여 이력 기록 실패");
            throw new ContributorRequiredException(KnowledgeMessage.CONTRIBUTOR_REQUIRED.getMessage());
        }

        if (type == null) {
            log.error("기여 타입이 null입니다 - 기여 이력 기록 실패");
            throw new ContributionTypeRequiredException(KnowledgeMessage.CONTRIBUTION_TYPE_REQUIRED.getMessage());
        }

        CardContribution contribution = CardContribution.builder()
                .card(card)
                .contributor(user)
                .contributionType(type)
                .description(description)
                .build();

        CardContribution saved = contributionRepository.save(contribution);
        log.info("기여 이력 기록 완료: contributionId={}, cardId={}, userId={}, type={}",
                saved.getId(), card.getId(), user.getId(), type);
    }
}
