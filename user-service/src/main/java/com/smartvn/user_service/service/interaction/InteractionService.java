package com.smartvn.user_service.service.interaction;

import com.smartvn.user_service.dto.interaction.InteractionExportDTO;
import com.smartvn.user_service.enums.InteractionType;
import com.smartvn.user_service.model.UserInteraction;
import com.smartvn.user_service.repository.UserInteractionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InteractionService {
    private final UserInteractionRepository interactionRepository;

    @Transactional
    public void recordInteraction(Long userId, Long productId, InteractionType type) {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        boolean exists = interactionRepository
                .existsByUserIdAndProductIdAndInteractionTypeAndCreatedAtAfter(
                        userId, productId, type, fiveMinutesAgo
                );

        if (exists) {
            log.debug("Duplicate interaction ignored");
            return;
        }

        UserInteraction interaction = new UserInteraction();
        interaction.setUserId(userId);
        interaction.setProductId(productId);
        interaction.setInteractionType(type);
        interaction.setWeight(type.getWeight());

        interactionRepository.save(interaction);
        log.info("Recorded {} interaction for user={}, product={}", type, userId, productId);
    }

    // Export cho AI
    public List<InteractionExportDTO> exportForAI() {
        return interactionRepository.findAll().stream()
                .map(i -> new InteractionExportDTO(
                        i.getUserId() != null ? i.getUserId().toString() : "guest",
                        i.getProductId().toString(),
                        i.getWeight(),
                        i.getInteractionType().name()
                ))
                .collect(Collectors.toList());
    }
}