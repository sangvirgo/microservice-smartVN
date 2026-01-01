package com.smartvn.admin_service.dto.ai;

import lombok.Data;

@Data
public class TrainingResponse {
    private String status;
    private String job_id;
    private String message;
}