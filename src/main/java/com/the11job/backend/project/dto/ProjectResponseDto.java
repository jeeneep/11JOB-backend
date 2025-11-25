package com.the11job.backend.project.dto;

import com.the11job.backend.file.service.FileService; // 🌟 FileService import 추가
import com.the11job.backend.project.entity.Project;
import lombok.Getter;
import lombok.NoArgsConstructor; // NoArgsConstructor 추가 (선택)

@Getter
@NoArgsConstructor // Lombok NoArgsConstructor 추가 (선택)
public class ProjectResponseDto {
    private Long id;
    private String title;
    private String description;
    private String startDate;
    private String endDate;
    private String linkUrl;
    private String imageUrl; // 완전한 URL을 포함

    // ----------------------------------------------------
    // URL 변환 로직이 적용된 새로운 생성자
    // ----------------------------------------------------
    /**
     * Project 엔티티와 FileService를 받아 DTO를 생성하는 생성자입니다.
     * DB 경로(Path)를 완전한 URL로 변환합니다.
     */
    public ProjectResponseDto(Project project, FileService fileService) {
        this.id = project.getId();
        this.title = project.getTitle();
        this.description = project.getDescription();
        this.startDate = project.getStartDate();
        this.endDate = project.getEndDate();
        this.linkUrl = project.getLinkUrl();

        // FileService를 사용하여 DB 경로를 완전한 URL로 변환
        String path = project.getImageUrl();
        this.imageUrl = fileService.convertToFullUrl(path);
    }
}