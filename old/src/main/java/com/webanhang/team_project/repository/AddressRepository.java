package com.webanhang.team_project.repository;

import com.webanhang.team_project.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    void deleteByUserId(Long UserId);
}
