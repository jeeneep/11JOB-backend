package com.the11job.backend.portfolio.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data // @Getter, @Setter, @ToString 등을 자동으로 만들어줍니다.
@NoArgsConstructor // JSON 데이터를 객체로 변환(역직렬화)할 때 기본 생성자가 필요합니다.
public class LinkDto {
    private String title;
    private String url;
}