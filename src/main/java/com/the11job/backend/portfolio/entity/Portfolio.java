package com.the11job.backend.portfolio.entity;

import com.the11job.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "portfolios")
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private String phone;
    private String address;
    private String profileImagePath;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PortfolioItem> items = new ArrayList<>();


    // == 생성자 (신규 생성 시) ==
    public Portfolio(User user, String phone, String address, String profileImagePath) {
        this.user = user;
        this.phone = phone;
        this.address = address;
        this.profileImagePath = profileImagePath;
    }

    // == 수정 시 정보 업데이트 ==
    public void updateInfo(String phone, String address, String profileImagePath) {
        this.phone = phone;
        this.address = address;
        if (profileImagePath != null) {
            this.profileImagePath = profileImagePath;
        }
    }

    public void clearChildLists() {
        this.items.clear();
    }

    public void addItem(PortfolioItem item) {
        this.items.add(item);
        item.setPortfolio(this);
    }
}