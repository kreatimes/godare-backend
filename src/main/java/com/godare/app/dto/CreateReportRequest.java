package com.godare.app.dto;

import lombok.Data;

@Data
public class CreateReportRequest {
    private String reportedId;
    private String type;
    private String reason;
    private String description;
}