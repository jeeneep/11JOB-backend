package com.the11job.backend.schedule.dto;

import com.the11job.backend.file.entity.File;
import com.the11job.backend.schedule.entity.Schedule;
import com.the11job.backend.schedule.entity.ScheduleDetail;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ScheduleResponse {

    // Schedule 기본 정보
    private Long scheduleId;
    private Long companyId;
    private String companyName;
    private String title;
    private LocalDate scheduleDate;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    // 1:N 관계 정보
    private List<ScheduleDetailResponse> details;
    private List<FileResponse> files;

    /**
     * Schedule 엔티티를 받아 DTO를 생성하는 생성자 (Details와 Files는 Stream을 통해 Response DTO로 변환)
     */
    public ScheduleResponse(Schedule schedule) {
        this.scheduleId = schedule.getId();
        this.companyId = schedule.getCompany().getId();
        this.companyName = schedule.getCompany().getName();
        this.title = schedule.getTitle();
        this.scheduleDate = schedule.getScheduleDate();
        this.createdDate = schedule.getCreatedDate();
        this.updatedDate = schedule.getUpdatedDate();

        // 1. ScheduleDetail 엔티티 목록을 Response DTO로 변환
        this.details = schedule.getDetails().stream()
                .map(ScheduleDetailResponse::new)
                .collect(Collectors.toList());

        // 2. File 엔티티 목록을 Response DTO로 변환
        this.files = schedule.getFiles().stream()
                .map(FileResponse::new)
                .collect(Collectors.toList());
    }

    // --- 내부 클래스: ScheduleDetail 응답 구조 ---
    @Getter
    @NoArgsConstructor
    public static class ScheduleDetailResponse {
        private Long detailId;
        private String title;
        private String content;

        public ScheduleDetailResponse(ScheduleDetail detail) {
            this.detailId = detail.getId();
            this.title = detail.getTitle();
            this.content = detail.getContent();
        }
    }

    // --- 내부 클래스: File 응답 구조 ---
    @Getter
    @NoArgsConstructor
    public static class FileResponse {
        private Long fileId;
        private String originalName;
        private String filePath; // 파일 접근 URL 또는 경로

        public FileResponse(File file) {
            this.fileId = file.getId();
            this.originalName = file.getOriginalName();
            this.filePath = file.getStoragePath();
        }
    }
}