package com.smartvn.user_service.repository;

import com.smartvn.user_service.enums.UserRole;
import com.smartvn.user_service.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Query("SELECT COUNT(u) FROM User u JOIN u.role r WHERE r.name = :roleName")
    long countByRoleName(@Param("roleName") UserRole roleName);

    long countByRole_Name(UserRole roleName);
    long countByIsBanned(boolean isBanned);
    long countByActive(boolean active);

    long countByCreatedAtAfter(LocalDateTime date);
}