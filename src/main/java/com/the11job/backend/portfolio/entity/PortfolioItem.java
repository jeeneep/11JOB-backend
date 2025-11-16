package com.the11job.backend.portfolio.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "portfolio_items") // 모든 항목이 이 테이블에 저장됨
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // 1. 상속 전략: 단일 테이블
@DiscriminatorColumn(name = "DTYPE") // 2. 타입을 구분할 컬럼
public abstract class PortfolioItem { // 3. 추상 클래스

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;
}