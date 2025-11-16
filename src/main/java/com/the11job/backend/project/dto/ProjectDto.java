package com.the11job.backend.project.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectDto {
    private String title;
    private String description;
    private String startDate;
    private String endDate;
    private String linkUrl;
}