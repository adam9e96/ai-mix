package com.aimix_aimixapi.common.uuid;

import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

/**
 * UUID v7 Generator 어노테이션
 * Hibernate 6.5+의 @IdGeneratorType을 사용하여 타입 안전한 방식으로 UUID v7 생성
 * 
 * 사용 예시:
 * <pre>
 * {@code
 * @Entity
 * public class MyEntity {
 *     @Id
 *     @UuidV7
 *     private UUID id;
 * }
 * }
 * </pre>
 */
@IdGeneratorType(UuidV7Generator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD, METHOD})
public @interface UuidV7 {
}
