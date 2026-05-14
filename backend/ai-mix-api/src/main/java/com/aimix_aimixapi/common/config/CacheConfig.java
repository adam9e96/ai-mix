package com.aimix_aimixapi.common.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

/**
 * Redis 캐시 설정
 * - @Cacheable, @CacheEvict 등 캐시 어노테이션 활성화
 * - 캐시 이름별로 TTL(만료 시간)을 다르게 설정
 * - JSON 직렬화로 캐시 데이터를 저장
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Redis 기반 CacheManager 설정
     * 캐시별 TTL:
     * - knowledgeCards: 10분 (자주 조회, 변경 빈도 낮음)
     * - knowledgeCardDetail: 5분 (상세 조회)
     * - knowledgeCardTop10: 30분 (TOP10 순위)
     * - userStats: 5분 (사용자 통계)
     * - 기본: 10분
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 기본 캐시 설정 (TTL: 10분)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // 캐시별 TTL 개별 설정
        Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
                "knowledgeCards", defaultConfig.entryTtl(Duration.ofMinutes(10)),
                "knowledgeCardDetail", defaultConfig.entryTtl(Duration.ofMinutes(5)),
                "knowledgeCardTop10", defaultConfig.entryTtl(Duration.ofMinutes(30)),
                "userStats", defaultConfig.entryTtl(Duration.ofMinutes(5))
        );

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
