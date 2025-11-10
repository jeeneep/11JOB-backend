package com.the11job.backend.portfolio.dto; // (패키지 경로는 예시입니다)

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class PortfolioRegistrationRequestDto {
    // 이름, 이메일은 User 엔티티에서 가져옴
    private String phone;
    private String address;

    private List<EducationDto> educations;
    private List<ExperienceDto> experiences;
    private List<ActivityDto> activities;
    private List<LinkDto> links;
    private List<CertificateDto> certificates;
}