package com.the11job.backend.schedule.repository;

import com.the11job.backend.schedule.entity.Schedule;
import com.the11job.backend.schedule.entity.ScheduleDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleDetailRepository extends JpaRepository<ScheduleDetail, Long> {

    /**
     * 특정 Schedule에 연결된 모든 ScheduleDetail 엔티티를 삭제합니다. ScheduleService의 updateScheduleDetails 메서드에서 기존 항목을 삭제할 때 사용됩니다. *
     * 💡 ScheduleService에서 @Transactional이 적용되어 있으므로 이 메서드도 트랜잭션 내에서 실행됩니다.
     */
    void deleteBySchedule(Schedule schedule);
}