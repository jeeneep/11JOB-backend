package com.the11job.backend.project.entity;

import com.the11job.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // User와 N:1 관계
    private User user;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String startDate;
    private String endDate;
    private String linkUrl;
    private String imageUrl; // (파일 저장 기능 없으므로 null로 저장됨)

    // 생성자 (Service에서 사용)
    public Project(String title, String description, String startDate, String endDate, String linkUrl, String imageUrl, User user) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.linkUrl = linkUrl;
        this.imageUrl = imageUrl;
        this.user = user;
    }

    /**
     * 수정(Update)을 위한 헬퍼 메서드
     */
    public void update(String title, String description, String startDate, String endDate, String linkUrl, String imageUrl) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.linkUrl = linkUrl;

        // 새 이미지가 있을 때만(null이 아닐 때만) 덮어쓰기
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
    }
}