package com.the11job.backend.file.entity;

import com.the11job.backend.global.entity.BaseEntity;
import com.the11job.backend.schedule.entity.Schedule;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class File extends BaseEntity {

    // 원본 파일명 (사용자 업로드 이름)
    @Column(name = "original_name", nullable = false)
    private String originalName;

    // S3에 저장된 실제 파일 경로 또는 키 (Unique)
    @Column(name = "storage_path", nullable = false, unique = true, length = 1000)
    private String storagePath;

    // 파일 타입 (예: image/png, application/pdf)
    @Column(name = "content_type")
    private String contentType;

    // Schedule 엔터티와 ManyToOne 관계 설정 (하나의 Schedule에 여러 파일 업로드 가능)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    // ScheduleService.addFile 편의 메서드를 위한 set 메서드
    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    @Builder
    public File(String originalName, String storagePath, String contentType, Schedule schedule) {
        this.originalName = originalName;
        this.storagePath = storagePath;
        this.contentType = contentType;
        this.schedule = schedule;
    }
}