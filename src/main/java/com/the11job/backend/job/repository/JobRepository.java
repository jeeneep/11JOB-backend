package com.the11job.backend.job.repository;

import com.the11job.backend.job.entity.Job;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    // requestNo(외부 고유 ID)로 Job 엔터티를 찾는 메서드
    Optional<Job> findByRequestNo(String requestNo);
}