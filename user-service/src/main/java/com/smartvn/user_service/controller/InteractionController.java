package com.smartvn.user_service.controller;

import com.smartvn.user_service.dto.interaction.InteractionRequestDTO;
import com.smartvn.user_service.dto.response.ApiResponse;
import com.smartvn.user_service.model.User;
import com.smartvn.user_service.repository.UserRepository;
import com.smartvn.user_service.security.jwt.JwtUtils;
import com.smartvn.user_service.service.interaction.InteractionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/interactions")
@RequiredArgsConstructor
@Slf4j
public class InteractionController {
    private final InteractionService interactionService;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    @PostMapping("/record")
    public ResponseEntity<ApiResponse<Void>> recordInteraction(
            @RequestHeader(value = "Authorization", required = false) String jwt,
            @RequestBody InteractionRequestDTO request) {

        Long userId = null;
        if (jwt != null && jwt.startsWith("Bearer ")) {
            try {
                String token = jwt.substring(7);
                String email = jwtUtils.getEmailFromToken(token);
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new EntityNotFoundException("User not found with email from JWT: " + email));
                userId = user.getId();
            } catch (Exception e) {
                log.debug("Guest user interaction");
            }
        }

        interactionService.recordInteraction(
                userId,
                request.getProductId(),
                request.getInteractionType()
        );

        return ResponseEntity.ok(ApiResponse.success(null, "Interaction recorded"));
    }
}