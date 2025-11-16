package com.the11job.backend.project.repository;

import com.the11job.backend.project.entity.Project;
import com.the11job.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    // "내" 프로젝트 목록 조회를 위한 메서드
    List<Project> findByUser(User user);
}