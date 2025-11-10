package com.the11job.backend.file.repository;

import com.the11job.backend.file.entity.File;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    // Schedule ID로 연결된 모든 File 엔티티를 조회
    List<File> findAllByScheduleId(Long scheduleId);
}