package com.the11job.backend.schedule.repository;

import com.the11job.backend.schedule.entity.Schedule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // 특정 사용자의 모든 일정을 조회
    List<Schedule> findAllByUserIdOrderByScheduleDateAsc(Long userId);

    // 특정 사용자의 특정 기업에 대한 일정을 조회
    List<Schedule> findAllByUserIdAndCompanyIdOrderByScheduleDateAsc(Long userId, Long companyId);
}