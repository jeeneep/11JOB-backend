package com.the11job.backend.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleDetailRequest {

    private Long detailId;

    @NotBlank(message = "세부 항목 제목은 필수입니다.")
    private String title;

    private String content;
}