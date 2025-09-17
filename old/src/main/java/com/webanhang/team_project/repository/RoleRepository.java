package com.webanhang.team_project.repository;

import com.webanhang.team_project.enums.UserRole;
import com.webanhang.team_project.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(UserRole role);
}