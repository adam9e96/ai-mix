package com.aimix_aimixapi.common.uuid;

import com.github.f4b6a3.uuid.UuidCreator;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;

/**
 * UUID v7 Generator
 * uuid-creator 라이브러리를 사용하여 타임스탬프 기반 UUID v7 생성
 * UUID v7의 장점:
 * - 타임스탬프 기반으로 정렬 가능
 * - 인덱스 성능이 더 좋음
 * - 생성 시각 정보 포함
 */
public class UuidV7Generator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        return UuidCreator.getTimeOrderedEpoch();
    }
}
