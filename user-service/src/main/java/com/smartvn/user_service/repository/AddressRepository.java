package com.smartvn.user_service.repository;

import com.smartvn.user_service.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    void deleteByUserId(Long UserId);
}
