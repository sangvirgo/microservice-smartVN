package com.smartvn.user_service.repository;

import com.smartvn.user_service.enums.UserRole;
import com.smartvn.user_service.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
    List<User> findByEmailContainingOrFirstNameContainingOrLastNameContainingAndRoleNameNot(
            String email, String firstName, String lastName, UserRole roleName, Pageable pageable);

    // Kết hợp tìm kiếm và lọc
    List<User> findByEmailContainingOrFirstNameContainingOrLastNameContainingAndRoleName(
            String email, String firstName, String lastName, UserRole roleName, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u JOIN u.role r WHERE r.name = :roleName")
    long countByRoleName(@Param("roleName") UserRole roleName);

}