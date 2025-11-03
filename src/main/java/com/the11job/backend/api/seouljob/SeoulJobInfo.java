package com.the11job.backend.api.seouljob;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;
import lombok.Getter;

@XmlRootElement(name = "GetJobInfo")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public class SeoulJobInfo {

    @XmlElement(name = "list_total_count")
    private String listTotalCount;

    @XmlElement(name = "RESULT")
    private Result result;

    // 리스트 요소에 nillable=true 추가 (List가 비어있을 때 안전하게 처리)
    @XmlElement(name = "row", nillable = true)
    private List<JobDetail> jobDetails;

    // --- Nested classes for XML structure ---

    @XmlRootElement(name = "RESULT")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Result {
        @XmlElement(name = "CODE")
        private String code;

        @XmlElement(name = "MESSAGE")
        private String message;

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    @XmlRootElement(name = "row")
    @XmlAccessorType(XmlAccessType.FIELD)
    @Getter
    public static class JobDetail {

        // 모든 필드에 nillable=true 추가하여 XML 데이터 누락/빈 값에 대응
        @XmlElement(name = "JO_REQST_NO", nillable = true)
        private String joRequestNo;

        @XmlElement(name = "JO_REGIST_NO", nillable = true)
        private String joRegisterNo;

        @XmlElement(name = "CMPNY_NM", nillable = true)
        private String companyName;

        @XmlElement(name = "BSNS_SUMRY_CN", nillable = true)
        private String businessSummary;

        @XmlElement(name = "RCRIT_JSSFC_CMMN_CODE_SE", nillable = true)
        private String recruitCode;

        @XmlElement(name = "JOBCODE_NM", nillable = true)
        private String jobCodeName;

        @XmlElement(name = "RCRIT_NMPR_CO", nillable = true)
        private String recruitNumber; // String 타입 유지

        @XmlElement(name = "ACDMCR_CMMN_CODE_SE", nillable = true)
        private String academicCode;

        @XmlElement(name = "ACDMCR_NM", nillable = true)
        private String academicName;

        @XmlElement(name = "EMPLYM_STLE_CMMN_CODE_SE", nillable = true)
        private String employmentTypeCode;

        @XmlElement(name = "EMPLYM_STLE_CMMN_MM", nillable = true)
        private String employmentType;

        @XmlElement(name = "WORK_PARAR_BASS_ADRES_CN", nillable = true)
        private String workAddress;

        @XmlElement(name = "SUBWAY_NM", nillable = true)
        private String subwayName;

        @XmlElement(name = "DTY_CN", nillable = true)
        private String dutyContent;

        @XmlElement(name = "CAREER_CND_CMMN_CODE_SE", nillable = true)
        private String careerConditionCode;

        @XmlElement(name = "CAREER_CND_NM", nillable = true)
        private String careerConditionName;

        @XmlElement(name = "HOPE_WAGE", nillable = true)
        private String hopeWage;

        @XmlElement(name = "RET_GRANTS_NM", nillable = true)
        private String retirementGrantsName;

        @XmlElement(name = "WORK_TIME_NM", nillable = true)
        private String workTimeName;

        @XmlElement(name = "WORK_TM_NM", nillable = true)
        private String workTime;

        @XmlElement(name = "HOLIDAY_NM", nillable = true)
        private String holidayName;

        @XmlElement(name = "WEEK_WORK_HR", nillable = true)
        private String weeklyWorkHours; // String 타입 유지

        @XmlElement(name = "JO_FEINSR_SBSCRB_NM", nillable = true)
        private String insuranceName;

        @XmlElement(name = "RCEPT_CLOS_NM", nillable = true)
        private String receiptClosingName;

        @XmlElement(name = "MODEL_MTH_NM", nillable = true)
        private String modelMethodName;

        @XmlElement(name = "RCEPT_MTH_NM", nillable = true)
        private String receiptMethodName;

        @XmlElement(name = "PRESENTN_PAPERS_NM", nillable = true)
        private String presentPapersName;

        @XmlElement(name = "MNGR_NM", nillable = true)
        private String managerName;

        @XmlElement(name = "MNGR_PHON_NO", nillable = true)
        private String managerPhoneNumber;

        @XmlElement(name = "MNGR_INSTT_NM", nillable = true)
        private String managerInstituteName;

        @XmlElement(name = "BASS_ADRES_CN", nillable = true)
        private String bassAddressContent;

        @XmlElement(name = "JO_SJ", nillable = true)
        private String jobSubject;

        @XmlElement(name = "JO_REG_DT", nillable = true)
        private String jobRegistrationDate;

        @XmlElement(name = "GUI_LN", nillable = true)
        private String guideLine;

    }
}