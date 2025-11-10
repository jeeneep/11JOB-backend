package com.the11job.backend.company.repository;

import com.the11job.backend.company.entity.Company;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /**
     * 기업명으로 Company 엔터티를 조회합니다. 채용 공고 저장 시 중복 체크 및 재활용에 사용됩니다.
     *
     * @param name 조회할 기업명
     * @return Company 엔터티 (Optional)
     */
    Optional<Company> findByName(String name);
}