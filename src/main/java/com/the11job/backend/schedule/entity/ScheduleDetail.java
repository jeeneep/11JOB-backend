package com.the11job.backend.schedule.entity;

import com.the11job.backend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Schedule 엔터티의 세부 사항 항목 (1:N 관계) 화면의 "세부 사항" 영역에서 '제목'과 '내용' 세트를 여러 개 저장하기 위함
 */
@Entity
@Table(name = "schedule_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleDetail extends BaseEntity {

    // Schedule 참조 (ManyToOne: 연관관계의 주인)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    @Setter // Schedule에서 편의 메서드를 통해 설정하기 위해 Setter 추가
    private Schedule schedule;

    // 세부 사항의 제목 (예: 예상 질문, 준비물)
    @Column(name = "detail_title", nullable = false, length = 500)
    private String title;

    // 세부 사항의 내용 (예: 질문 목록, 내용)
    @Lob // 대용량 텍스트 저장을 위해 @Lob 사용
    @Column(name = "detail_content")
    private String content;

    // ----------------------------------------------------
    // Constructor and Builder
    // ----------------------------------------------------

    @Builder
    public ScheduleDetail(Schedule schedule, String title, String content) {
        this.schedule = schedule;
        this.title = title;
        this.content = content;
    }

    // ----------------------------------------------------
    // Update Method (세부 사항 갱신)
    // ----------------------------------------------------

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}