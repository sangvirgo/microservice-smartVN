package com.smartvn.admin_service.controller;

import com.smartvn.admin_service.client.ProductServiceClient;
import com.smartvn.admin_service.dto.ai.InteractionExportDTO;
import com.smartvn.admin_service.dto.ai.ProductExportDTO;
import com.smartvn.admin_service.service.DataExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/internal/admin/export")
@RequiredArgsConstructor
public class InternalAdminController {
    private final DataExportService dataExportService;
    private final ProductServiceClient productServiceClient;

    // AI service gọi để lấy ALL interactions (merged)
    @GetMapping("/interactions")
    public ResponseEntity<List<InteractionExportDTO>> exportInteractions() {
        List<InteractionExportDTO> data = dataExportService.aggregateAllInteractions();
        return ResponseEntity.ok(data);
    }

    // AI service gọi để lấy products (forward request)
    @GetMapping("/products")
    public ResponseEntity<List<ProductExportDTO>> exportProducts() {
        List<ProductExportDTO> products = productServiceClient.exportProducts();
        return ResponseEntity.ok(products);
    }
}