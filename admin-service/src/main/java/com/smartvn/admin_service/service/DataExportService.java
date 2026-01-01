package com.smartvn.admin_service.service;

import com.smartvn.admin_service.client.OrderServiceClient;
import com.smartvn.admin_service.client.UserServiceClient;
import com.smartvn.admin_service.dto.ai.InteractionExportDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataExportService {
    private final UserServiceClient userServiceClient;
    private final OrderServiceClient orderServiceClient;

    public List<InteractionExportDTO> aggregateAllInteractions() {
        List<InteractionExportDTO> allInteractions = new ArrayList<>();

        // 1. Lấy user interactions (CLICK, VIEW, ADD_TO_CART)
        try {
            List<InteractionExportDTO> userInteractions =
                    userServiceClient.exportUserInteractions();
            allInteractions.addAll(userInteractions);
            log.info("Fetched {} user interactions", userInteractions.size());
        } catch (Exception e) {
            log.error("Failed to fetch user interactions", e);
        }

        // 2. Lấy order interactions (PURCHASE)
        try {
            List<InteractionExportDTO> orderInteractions =
                    orderServiceClient.exportOrderInteractions();
            allInteractions.addAll(orderInteractions);
            log.info("Fetched {} order interactions", orderInteractions.size());
        } catch (Exception e) {
            log.error("Failed to fetch order interactions", e);
        }

        // 3. Loại bỏ duplicates (nếu cần)
        return allInteractions;
    }
}
