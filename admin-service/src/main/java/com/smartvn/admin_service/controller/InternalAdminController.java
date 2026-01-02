package com.smartvn.admin_service.controller;

import com.smartvn.admin_service.client.ProductServiceClient;
import com.smartvn.admin_service.dto.ai.InteractionExportDTO;
import com.smartvn.admin_service.dto.ai.ProductExportDTO;
import com.smartvn.admin_service.service.DataExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/internal/admin")
@RequiredArgsConstructor
public class InternalAdminController {
    private final DataExportService dataExportService;
    private final ProductServiceClient productServiceClient;

    @GetMapping("/export/products")
    public ResponseEntity<List<ProductExportDTO>> exportProducts(
            @RequestHeader("X-API-KEY") String apiKey
    ) {
        // Validate API key
        if (!apiKey.equals("a-very-secret-key-for-internal-communication")) {
            return ResponseEntity.status(401).build();
        }

        List<ProductExportDTO> products = dataExportService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/export/interactions")
    public ResponseEntity<List<InteractionExportDTO>> exportInteractions(
            @RequestHeader("X-API-KEY") String apiKey
    ) {
        if (!apiKey.equals("a-very-secret-key-for-internal-communication")) {
            return ResponseEntity.status(401).build();
        }

        List<InteractionExportDTO> interactions = dataExportService.aggregateAllInteractions();
        return ResponseEntity.ok(interactions);
    }

}