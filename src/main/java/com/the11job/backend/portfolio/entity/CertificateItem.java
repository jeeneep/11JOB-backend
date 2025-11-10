package com.the11job.backend.portfolio.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("CERTIFICATE")
public class CertificateItem extends PortfolioItem {

    private String title;
    private String acquireDate;

    public CertificateItem(String title, String acquireDate) {
        this.title = title;
        this.acquireDate = acquireDate;
    }
}