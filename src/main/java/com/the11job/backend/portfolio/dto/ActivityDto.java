package com.the11job.backend.portfolio.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ActivityDto {
    private String institutionName; // 기관명
    private String startDate;       // 시작년월
    private String endDate;         // 종료/졸업년월
}