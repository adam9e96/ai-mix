package com.aimix_aimixapi.user.repository;

import com.aimix_aimixapi.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);

    /**
     * 닉네임으로 사용자 조회
     */
    Optional<User> findByNickname(String nickname);
}
