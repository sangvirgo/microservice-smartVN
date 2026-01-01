package com.smartvn.admin_service.dto.ai;

import lombok.Data;

@Data
public class TrainingRequest {
    private Boolean force_retrain_all = false;
    private String model_version_tag;
}