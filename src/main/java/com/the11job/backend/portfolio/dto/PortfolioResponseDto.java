package com.the11job.backend.portfolio.dto;

import com.the11job.backend.portfolio.entity.ActivityItem;
import com.the11job.backend.portfolio.entity.CertificateItem;
import com.the11job.backend.portfolio.entity.EducationItem;
import com.the11job.backend.portfolio.entity.ExperienceItem;
import com.the11job.backend.portfolio.entity.LinkItem;
import com.the11job.backend.portfolio.entity.Portfolio;
import com.the11job.backend.portfolio.entity.PortfolioItem;
import lombok.Getter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PortfolioResponseDto {

    // 1. Portfolio의 기본 정보
    private Long id;
    private String phone;
    private String address;
    private String profileImagePath;

    // 2. User 정보 (민감 정보 제외)
    private UserDto user;

    private List<EducationItemDto> educations;
    private List<ExperienceItemDto> experiences; // (추가)
    private List<ActivityItemDto> activities;   // (추가)
    private List<LinkItemDto> links;
    private List<CertificateItemDto> certificates;

    // 4. 엔티티 -> DTO 변환 생성자
    public PortfolioResponseDto(Portfolio portfolio) {
        this.id = portfolio.getId();
        this.phone = portfolio.getPhone();
        this.address = portfolio.getAddress();
        this.profileImagePath = portfolio.getProfileImagePath();

        if (portfolio.getUser() != null) {
            this.user = new UserDto(portfolio.getUser());
        }


        this.educations = portfolio.getItems().stream()
                .filter(item -> item instanceof EducationItem) // 타입 검사
                .map(item -> new EducationItemDto((EducationItem) item)) // DTO로 변환
                .collect(Collectors.toList());

        this.experiences = portfolio.getItems().stream()
                .filter(item -> item instanceof ExperienceItem)
                .map(item -> new ExperienceItemDto((ExperienceItem) item))
                .collect(Collectors.toList());

        this.activities = portfolio.getItems().stream()
                .filter(item -> item instanceof ActivityItem)
                .map(item -> new ActivityItemDto((ActivityItem) item))
                .collect(Collectors.toList());

        this.links = portfolio.getItems().stream()
                .filter(item -> item instanceof LinkItem)
                .map(item -> new LinkItemDto((LinkItem) item))
                .collect(Collectors.toList());

        this.certificates = portfolio.getItems().stream()
                .filter(item -> item instanceof CertificateItem)
                .map(item -> new CertificateItemDto((CertificateItem) item))
                .collect(Collectors.toList());
    }

    // --- 내부 DTO 클래스들 ---

    @Getter
    private static class UserDto {
        private Long id;
        private String email;
        private String name;

        // User 엔티티에서 민감정보(비밀번호 등)를 제외하고 복사
        UserDto(com.the11job.backend.user.entity.User user) {
            this.id = user.getId();
            this.email = user.getEmail();
            this.name = user.getName();
        }
    }

    @Getter
    private static class EducationItemDto {
        private String institutionName;
        private String startDate;
        private String endDate;

        EducationItemDto(EducationItem item) {
            this.institutionName = item.getInstitutionName();
            this.startDate = item.getStartDate();
            this.endDate = item.getEndDate();
        }
    }

    @Getter
    private static class ExperienceItemDto {
        private String institutionName;
        private String startDate;
        private String endDate;

        ExperienceItemDto(ExperienceItem item) {
            this.institutionName = item.getInstitutionName();
            this.startDate = item.getStartDate();
            this.endDate = item.getEndDate();
        }
    }

    @Getter
    private static class ActivityItemDto {
        private String institutionName;
        private String startDate;
        private String endDate;

        ActivityItemDto(ActivityItem item) {
            this.institutionName = item.getInstitutionName();
            this.startDate = item.getStartDate();
            this.endDate = item.getEndDate();
        }
    }

    @Getter
    private static class LinkItemDto {
        private String title;
        private String url;

        LinkItemDto(LinkItem item) {
            this.title = item.getTitle();
            this.url = item.getUrl();
        }
    }

    @Getter
    private static class CertificateItemDto {
        private String title;
        private String acquireDate;

        CertificateItemDto(CertificateItem item) {
            this.title = item.getTitle();
            this.acquireDate = item.getAcquireDate();
        }
    }
}