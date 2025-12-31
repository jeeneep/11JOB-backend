package com.the11job.backend.project.repository;

import com.the11job.backend.project.entity.Project;
import com.the11job.backend.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    // "내" 프로젝트 목록 조회를 위한 메서드
    List<Project> findByUser(User user);

    List<Project> findAllByUser(User user);
}