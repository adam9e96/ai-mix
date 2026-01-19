package com.aimix_aimixapi.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "userProfile")  // 추가
@Builder
public class User {

    /**
     * 기본 키 (SERIAL → PostgreSQL에서는 IDENTITY 전략과 매핑)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // SERIAL
    private Long id;

    /**
     * 유저 이메일 (로그인 ID로 사용)
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * 암호화된 비밀번호
     */
    @Column(nullable = false)
    private String password;

    /**
     * 유저 닉네임 (유니크)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    /**
     * 생년월일
     */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /**
     * 개인정보 동의 여부
     */
    @Column(name = "is_agreed", nullable = false)
    @Builder.Default
    private boolean isAgreed = false;

    /**
     * USER / ADMIN 등 권한 정보 (Enum)
     * PostgreSQL user_role enum 타입과 직접 매핑
     * @JdbcTypeCode(SqlTypes.NAMED_ENUM)를 사용하여 PostgreSQL enum 타입을 직접 매핑
     */
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "user_role")
    @Builder.Default
    private Role role = Role.USER;

    /**
     * 생성 시각
     */
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp  // Hibernate 어노테이션
    private LocalDateTime createdAt;

    /**
     * 마지막 로그인 시각
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * User 기준 - UserProfile과 1:1 연관관계
     * <p>
     * mappedBy = "user" → UserProfile 엔티티의 user 필드가 연관관계 주인 (외래키 보유, UserProfile.user 필드가 주인)
     * cascade = CascadeType.ALL → User를 저장/삭제하면 UserProfile도 같이 처리됨
     * orphanRemoval = true → UserProfile 참조를 제거하면 DB에서도 삭제됨 (고아 객체 삭제)
     * fetch = FetchType.LAZY → 필요할 때만 프로필 정보를 로딩
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private UserProfile userProfile;

    /**
     * 연관관계 편의 메서드
     * UserProfile.setUser(this) 를 자동 호출해서 두 객체 간의 일관성 유지
     */
    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
        if (userProfile != null && userProfile.getUser() != this) {
            userProfile.setUser(this);
        }
    }
}