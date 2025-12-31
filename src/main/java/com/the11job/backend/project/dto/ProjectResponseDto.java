package com.the11job.backend.project.dto;

import com.the11job.backend.project.entity.Project;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProjectResponseDto {
    private Long id;
    private String title;
    private String description;
    private String startDate;
    private String endDate;
    private String linkUrl;
    private String imageUrl; // 완전한 URL을 포함

    public ProjectResponseDto(Project project) {
        this.id = project.getId();
        this.title = project.getTitle();
        this.description = project.getDescription();
        this.startDate = project.getStartDate();
        this.endDate = project.getEndDate();
        this.linkUrl = project.getLinkUrl();

        this.imageUrl = project.getImageUrl();
    }
}