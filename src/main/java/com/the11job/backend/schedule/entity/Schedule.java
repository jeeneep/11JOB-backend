// src/main/java/com/the11job.backend.schedule.entity/Schedule.java
package com.the11job.backend.schedule.entity;

import com.the11job.backend.company.entity.Company;
import com.the11job.backend.file.entity.File;
import com.the11job.backend.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    // 1. 사용자 정보 (TODO: User 엔터티가 구현되면 연결)
    // 현재는 사용자 ID를 임시로 Long 타입으로 가정합니다.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 2. 기업 정보 (ManyToOne - Company 엔터티와 연결)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // 3. 캘린더 일정 정보

    // 일정 제목 (캘린더에 나올 일정 제목)
    @Column(name = "title", nullable = false)
    private String title;

    // 일정 예정일 (날짜만)
    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;

    // (선택) 일정 시간 (시간까지 필요하다면)
    @Column(name = "schedule_time")
    private LocalDateTime scheduleTime;

    // 4. 상세 내용 및 자료 정보 (첨부 이미지 참고)

    // 상세 제목 (예: 예상 질문)
    @Column(name = "detail_title", length = 500)
    private String detailTitle;

    // 상세 내용 (예: 예상 질문 내용, 자소서 초안 등)
    @Lob // 대용량 텍스트 저장을 위해 @Lob 사용
    @Column(name = "detail_content")
    private String detailContent;

    // 🌟 5. 다중 파일 경로 (One-to-Many - File 엔터티 리스트로 대체) 🌟
    // mappedBy는 연관 관계의 주인이 File 엔터티의 schedule 필드임을 명시
    // CascadeType.ALL은 Schedule 삭제 시 연결된 파일 메타데이터도 함께 삭제
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<File> files = new ArrayList<>(); // Null 방지를 위해 초기화

    // ----------------------------------------------------
    // Constructor and Builder
    // ----------------------------------------------------

    @Builder
    public Schedule(Long userId, Company company, String title, LocalDate scheduleDate,
                    LocalDateTime scheduleTime, String detailTitle, String detailContent) {
        this.userId = userId;
        this.company = company;
        this.title = title;
        this.scheduleDate = scheduleDate;
        this.scheduleTime = scheduleTime;
        this.detailTitle = detailTitle;
        this.detailContent = detailContent;
    }

    // ----------------------------------------------------
    // Update Method (일정 갱신)
    // ----------------------------------------------------

    public void update(String title, LocalDate scheduleDate, LocalDateTime scheduleTime,
                       String detailTitle, String detailContent) {
        this.title = title;
        this.scheduleDate = scheduleDate;
        this.scheduleTime = scheduleTime;
        this.detailTitle = detailTitle;
        this.detailContent = detailContent;
    }
}