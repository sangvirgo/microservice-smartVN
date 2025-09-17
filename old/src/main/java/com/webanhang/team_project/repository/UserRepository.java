package com.webanhang.team_project.repository;


import com.webanhang.team_project.enums.UserRole;
import com.webanhang.team_project.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.orders WHERE u.id = :userId")
    Optional<User> findUserWithOrders(@Param("userId") Long userId);

    User findByEmail(String email);
    List<User> findByRoleNameNot(UserRole roleName, Pageable pageable);
    List<User> findByEmailContainingOrFirstNameContainingOrLastNameContainingAndRoleNameNot(
            String email, String firstName, String lastName, UserRole roleName, Pageable pageable);

    // Lọc người dùng theo role
    List<User> findByRoleName(UserRole roleName, Pageable pageable);

    // Kết hợp tìm kiếm và lọc
    List<User> findByEmailContainingOrFirstNameContainingOrLastNameContainingAndRoleName(
            String email, String firstName, String lastName, UserRole roleName, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u JOIN u.role r WHERE r.name = :roleName")
    long countByRoleName(@Param("roleName") UserRole roleName);
}
