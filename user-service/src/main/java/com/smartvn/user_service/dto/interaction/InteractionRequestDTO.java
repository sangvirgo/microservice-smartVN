package com.smartvn.user_service.dto.interaction;

import com.smartvn.user_service.enums.InteractionType;
import lombok.Data;

@Data
public class InteractionRequestDTO {
    private Long productId;
    private InteractionType interactionType;
}