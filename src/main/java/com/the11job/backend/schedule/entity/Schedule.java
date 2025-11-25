// src/main/java/com/the11job.backend.schedule.entity/Schedule.java
package com.the11job.backend.schedule.entity;

import com.the11job.backend.company.entity.Company;
import com.the11job.backend.file.entity.File;
import com.the11job.backend.global.entity.BaseEntity;
import com.the11job.backend.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule extends BaseEntity {

    // 1. 사용자 정보 (ManyToOne - User 엔터티와 연결)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 2. 기업 정보 (ManyToOne - Company 엔터티와 연결)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // 3. 캘린더 일정 정보

    // 일정 제목 (캘린더에 나올 일정 제목)
    @Column(name = "title", nullable = false)
    private String title;

    // 일정 예정일 (날짜)
    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;

    // 4. 상세 내용 및 자료 정보 (첨부 이미지 참고)

    // 세부 사항 항목 리스트 (1:N 관계 추가)
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleDetail> details = new ArrayList<>(); // Null 방지를 위해 초기화

    // 다중 파일 경로 (One-to-Many - File 엔터티 리스트로 대체)
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<File> files = new ArrayList<>(); // Null 방지를 위해 초기화

    // ----------------------------------------------------
    // Constructor and Builder
    // ----------------------------------------------------

    @Builder
    public Schedule(User user, Company company, String title, LocalDate scheduleDate) {
        this.user = user;
        this.company = company;
        this.title = title;
        this.scheduleDate = scheduleDate;
    }

    // 양쪽 관계를 동시에 설정하여 동기화하는 편의 메서드
    public void addFile(File file) {
        this.files.add(file);
        if (file.getSchedule() != this) {
            file.setSchedule(this);
        }
    }

    // ----------------------------------------------------
    // 연관관계 편의 메서드 (ScheduleDetail 추가/제거)
    // ----------------------------------------------------

    public void addDetail(ScheduleDetail detail) {
        this.details.add(detail);
        if (detail.getSchedule() != this) {
            detail.setSchedule(this);
        }
    }

    // ----------------------------------------------------
    // Update Method (일정 갱신)
    // ----------------------------------------------------

    public void update(String title, LocalDate scheduleDate) {
        this.title = title;
        this.scheduleDate = scheduleDate;
        // 세부 내용(details)과 파일(files)은 별도의 Service 메서드에서 관리
    }
}