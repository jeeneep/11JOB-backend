package com.the11job.backend.portfolio.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("LINK")
public class LinkItem extends PortfolioItem {

    private String title;
    private String url;

    public LinkItem(String title, String url) {
        this.title = title;
        this.url = url;
    }
}