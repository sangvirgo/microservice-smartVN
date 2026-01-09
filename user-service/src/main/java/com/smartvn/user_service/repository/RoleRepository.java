package com.smartvn.user_service.repository;

import com.smartvn.user_service.enums.UserRole;
import com.smartvn.user_service.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(UserRole role);
}