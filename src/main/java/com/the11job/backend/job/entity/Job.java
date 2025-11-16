package com.the11job.backend.job.entity;

import com.the11job.backend.company.entity.Company;
import com.the11job.backend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
// 외부 API 고유 ID(requestNo)에 인덱스를 걸어 조회 성능을 높이고, 중복 저장 방지
@Table(name = "jobs", indexes = {
        @jakarta.persistence.Index(name = "idx_request_no", columnList = "request_no")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 사용을 위한 기본 생성자
public class Job extends BaseEntity {

    // Company 엔터티와의 관계 설정 (ManyToOne) - 필수 필드
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false) // FK 컬럼 이름
    private Company company;

    // 채용공고 관리번호 (API 응답: JO_REQST_NO) - UNIQUE 제약조건 추가
    @Column(name = "request_no", unique = true, nullable = false)
    private String requestNo;

    // 채용 제목 (API 응답: JO_SJ)
    @Column(name = "title", nullable = false)
    private String title;

    // 근무지 주소 (API 응답: WORK_PARAR_BASS_ADRES_CN)
    @Column(name = "work_address", nullable = false)
    private String workAddress;

    // 직무 이름 (API 응답: JOBCODE_NM)
    @Column(name = "job_code_name", nullable = false)
    private String jobCodeName;

    // 학력 조건 (API 응답: ACDMCR_NM)
    @Column(name = "academic_name", nullable = false)
    private String academicName;

    // 경력 조건 (API 응답: CAREER_CND_NM)
    @Column(name = "career_name", nullable = false)
    private String careerName;

    // 공고 등록일 (API 응답: REG_DT) - 정렬에 필수
    @Column(name = "registration_date")
    private LocalDate registrationDate;

    // 공고 마감일 (API에 없지만 일반적인 공고 엔터티에 추가하여 확장성 확보)
    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    // 이 공고의 원본 상세 페이지 URL
    @Column(name = "detail_url")
    private String detailUrl;

    // ----------------------------------------------------
    // Constructor and Builder
    // ----------------------------------------------------

    @Builder // 이 생성자에 Builder 부여
    public Job(Company company, String requestNo, String title, String workAddress,
               String jobCodeName, String academicName, String careerName,
               LocalDate registrationDate, LocalDate expirationDate, String detailUrl) {
        // Company는 JobSaverService에서 setCompany()를 통해 별도로 주입됩니다.
        this.requestNo = requestNo;
        this.title = title;
        this.workAddress = workAddress;
        this.jobCodeName = jobCodeName;
        this.academicName = academicName;
        this.careerName = careerName;
        this.registrationDate = registrationDate;
        this.expirationDate = expirationDate;
        this.detailUrl = detailUrl;
    }

    // ----------------------------------------------------
    // Business Logic - 데이터 갱신 메서드
    // ----------------------------------------------------

    /**
     * 외부 API로부터 받은 최신 데이터로 엔터티의 내용 갱신 requestNo와 ID는 갱신하지 않음
     *
     * @param updatedJob 최신 정보가 담긴 Job 객체 (Company 필드는 null 상태로 넘어옴)
     */
    public void update(Job updatedJob) {
        // Company는 갱신하지 않음 (JobSaverService에서 트랜잭션 내에서 설정됨)
        // companyName, hopeWage, weeklyWorkHours 등 중복 필드 제거
        this.title = updatedJob.getTitle();
        this.workAddress = updatedJob.getWorkAddress();
        this.jobCodeName = updatedJob.getJobCodeName();
        this.academicName = updatedJob.getAcademicName();
        this.careerName = updatedJob.getCareerName();
        this.registrationDate = updatedJob.getRegistrationDate();
        this.expirationDate = updatedJob.getExpirationDate();
        this.detailUrl = updatedJob.getDetailUrl();
        // BaseEntity의 updatedDate는 자동으로 갱신
    }


    /**
     * JobSaverService에서 Company를 연결하기 위한 Setter
     */
    public void setCompany(Company company) {
        this.company = company;
    }

}