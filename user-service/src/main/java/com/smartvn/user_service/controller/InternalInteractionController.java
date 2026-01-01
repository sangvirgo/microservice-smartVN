package com.smartvn.user_service.controller;

import com.smartvn.user_service.dto.interaction.InteractionExportDTO;
import com.smartvn.user_service.service.interaction.InteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/internal/user/export")
@RequiredArgsConstructor
public class InternalInteractionController {
    private final InteractionService interactionService;

    @GetMapping("/interactions")
    public ResponseEntity<List<InteractionExportDTO>> exportInteractions() {
        List<InteractionExportDTO> data = interactionService.exportForAI();
        return ResponseEntity.ok(data);
    }
}