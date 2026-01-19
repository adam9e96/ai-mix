package com.aimix_aimixapi.user.repository;

import com.aimix_aimixapi.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
