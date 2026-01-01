package com.smartvn.user_service.repository;

import com.smartvn.user_service.enums.InteractionType;
import com.smartvn.user_service.model.UserInteraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {

    // Check duplicate trong 5 phút
    boolean existsByUserIdAndProductIdAndInteractionTypeAndCreatedAtAfter(
            Long userId,
            Long productId,
            InteractionType type,
            LocalDateTime after
    );

    // Lấy tất cả interactions (cho AI export)
    List<UserInteraction> findAll();
}