package com.example.user_service.repositories;

import com.example.user_service.models.RoleType;
import com.example.user_service.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    @Query("SELECT DISTINCT u.role FROM UserEntity u")
    List<RoleType> findAllRoles();

    UserEntity findByVerificationToken(String verificationToken);

    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.isVerified = false")
    long countUnverifiedUsers();

    @Modifying
    @Transactional
    @Query("DELETE FROM UserEntity u WHERE u.isVerified = false")
    void deleteUnverifiedUsers();
}
