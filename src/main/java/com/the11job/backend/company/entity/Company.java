package com.the11job.backend.company.entity;

import com.the11job.backend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "companies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends BaseEntity {

    // 기업명: Unique 제약조건으로 중복 등록을 방지하고 검색 키로 사용
    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Builder
    public Company(String name) {
        this.name = name;
    }

}