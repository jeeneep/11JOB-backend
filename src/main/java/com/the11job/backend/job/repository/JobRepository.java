// src/main/java/com.the11job.backend.job.repository/JobRepository.java
package com.the11job.backend.job.repository;

import com.the11job.backend.job.entity.Job;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JobRepositoryCustom {

    Optional<Job> findByRequestNo(String requestNo);
}