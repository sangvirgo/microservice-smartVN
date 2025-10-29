package com.smartvn.user_service.repository;

import com.smartvn.user_service.model.Address;
import com.smartvn.user_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    void deleteByUserId(Long UserId);

    boolean existsByIdAndUserId(Long id, Long userId);

    Long user(User user);

    List<Address> findByUserIdAndIsActiveTrue(Long userId);
}
