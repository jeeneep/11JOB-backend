package com.the11job.backend.portfolio.repository;

import com.the11job.backend.portfolio.entity.Portfolio;
import com.the11job.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    Optional<Portfolio> findByUser(User user);

    @Query("SELECT p FROM Portfolio p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH p.items " + // 단일 리스트만 페치
            "WHERE p.id = :id")
    Optional<Portfolio> findByIdWithDetails(@Param("id") Long id);
}