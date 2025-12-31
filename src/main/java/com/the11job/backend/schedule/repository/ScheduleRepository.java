// src/main/java/com/the11job.backend.schedule.repository/ScheduleRepository.java (수정 필요)

package com.the11job.backend.schedule.repository;

import com.the11job.backend.schedule.entity.Schedule;
import com.the11job.backend.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // User 엔티티 객체를 직접 받아 일정을 조회하는 메서드
    List<Schedule> findAllByUserOrderByScheduleDateAsc(User user);

    List<Schedule> findAllByUser(User user);
}