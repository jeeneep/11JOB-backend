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

    // Company 엔터티와의 관계 설정 (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false) // FK 컬럼 이름
    private Company company;

    // 1. 채용공고 관리번호 (API 응답: JO_REQST_NO) - UNIQUE 제약조건 추가
    @Column(name = "request_no", unique = true, nullable = false)
    private String requestNo;

    // 회사명 (API 응답: CMPNY_NM) -> Company 엔터티에 위임하는 것이 이상적
    @Column(name = "company_name", nullable = false)
    private String companyName;

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

    // 희망 급여 (API 응답: HOPE_WAGE) - nullable=true 가능 (급여 정보가 없는 공고가 있을 경우)
    @Column(name = "hope_wage")
    private String hopeWage;

    // 주간 근무 시간 (API 응답: WEEK_WORK_HR) - Integer 타입 유지 (DB에는 INT로 저장됨)
    @Column(name = "weekly_work_hours")
    private Integer weeklyWorkHours;

    // 2. 등록일 (API 응답: JO_REG_DT) - String 대신 LocalDate 타입으로 변경
    // 날짜 연산 및 정렬에 유리하며 DB에는 DATE 타입으로 저장됨
    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    // 공고 마감일 (API에 없지만 일반적인 공고 엔터티에 추가하여 확장성 확보)
    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    // 이 공고의 원본 상세 페이지 URL (추가)
    @Column(name = "detail_url")
    private String detailUrl;

    // ----------------------------------------------------
    // Constructor and Builder
    // ----------------------------------------------------

    @Builder // 이 생성자에 Builder 부여
    public Job(String requestNo, String companyName, String title, String workAddress,
               String jobCodeName, String academicName, String careerName, String hopeWage,
               Integer weeklyWorkHours, LocalDate registrationDate, LocalDate expirationDate,
               String detailUrl) {
        // this.company = null; // Company는 Mapper에서 처리하지 않으므로 초기화하지 않음
        this.requestNo = requestNo;
        this.companyName = companyName;
        this.title = title;
        this.workAddress = workAddress;
        this.jobCodeName = jobCodeName;
        this.academicName = academicName;
        this.careerName = careerName;
        this.hopeWage = hopeWage;
        this.weeklyWorkHours = weeklyWorkHours;
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
     * @param updatedJob 최신 정보가 담긴 Job 객체
     */
    public void update(Job updatedJob) {
        // Company는 갱신하지 않음 (RequestNo 기반으로 이미 연결된 상태)
        this.companyName = updatedJob.getCompanyName();
        this.title = updatedJob.getTitle();
        this.workAddress = updatedJob.getWorkAddress();
        this.jobCodeName = updatedJob.getJobCodeName();
        this.academicName = updatedJob.getAcademicName();
        this.careerName = updatedJob.getCareerName();
        this.hopeWage = updatedJob.getHopeWage();
        this.weeklyWorkHours = updatedJob.getWeeklyWorkHours();
        // 등록일은 보통 변하지 않지만, API 정책에 따라 갱신될 수 있다면 포함
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