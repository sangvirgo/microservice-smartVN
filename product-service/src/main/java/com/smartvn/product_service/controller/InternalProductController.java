package com.smartvn.product_service.controller;


import com.smartvn.product_service.dto.InventoryCheckRequest;
import com.smartvn.product_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("${api.prefix}/internal/inventory")
public class InternalProductController {
    private final InventoryService  inventoryService;

    @PostMapping("//batch-check")
    public ResponseEntity<Map<String, Boolean>> batchCheckInventory(
            @RequestBody List<InventoryCheckRequest> requests) {

        Map<String, Boolean> results = new HashMap<>();

        for (InventoryCheckRequest req : requests) {
            String key = req.getProductId() + "-" + req.getSize();
            boolean hasStock = inventoryService.checkInventoryAvailability(req);
            results.put(key, hasStock);
        }

        return ResponseEntity.ok(results);
    }

    @PostMapping("/batch-reduce")
    public ResponseEntity<Void> batchReduceInventory(
            @RequestBody List<InventoryCheckRequest> requests) {

        inventoryService.batchReduceInventory(requests);
        return ResponseEntity.ok().build();
    }
}
