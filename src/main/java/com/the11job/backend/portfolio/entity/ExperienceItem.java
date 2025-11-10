package com.the11job.backend.portfolio.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("Experience")
public class ExperienceItem extends PortfolioItem {

    private String institutionName;
    private String startDate;
    private String endDate;

    // Service에서 DTO를 변환할 때 사용
    public ExperienceItem(String institutionName, String startDate, String endDate) {
        this.institutionName = institutionName;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}