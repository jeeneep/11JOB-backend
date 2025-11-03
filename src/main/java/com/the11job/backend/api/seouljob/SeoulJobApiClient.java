package com.the11job.backend.api.seouljob;

import com.the11job.backend.global.exception.ApiClientException;
import com.the11job.backend.global.exception.ErrorCode;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 서울시 일자리 정보 API 클라이언트 (WebClient 및 Jakarta EE 기반)
 */
@Slf4j
@Component
public class SeoulJobApiClient {

    // WebClient 주입 (Thread-safe하며, 비동기 처리에 유리)
    private final WebClient webClient;

    // 설정 파일에서 API 키와 URL 주입받기 (@Value 사용)
    @Value("${api.seoul.job.key}")
    private String API_KEY;

    // API 주소에서 {start}/{end}는 호출 시점에 동적으로 채우기 위해 제거
    private final String BASE_URL = "http://openapi.seoul.go.kr:8088";
    private final String API_PATH = "/{API_KEY}/xml/GetJobInfo/{start}/{end}";

    /**
     * WebClient.Builder를 주입받아 설정 후 WebClient 인스턴스 생성 WebClient의 기본 버퍼 제한(256KB)을 늘려 대용량 XML 응답을 처리할 수 있도록 설정
     */
    public SeoulJobApiClient(WebClient.Builder webClientBuilder) {

        // 최대 버퍼 크기를 4MB로 설정 (1000건의 XML 데이터 수신 대비)
        final int size = 1024 * 1024 * 4; // 4MB
        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
                .build();

        this.webClient = webClientBuilder
                .baseUrl(BASE_URL)
                .exchangeStrategies(strategies) // 설정된 strategies 적용
                .build();
    }

    /**
     * 서울시 일자리 정보를 조회
     *
     * @param startIndex 시작 인덱스
     * @param endIndex   끝 인덱스
     * @return 조회된 JobInfo 객체를 담은 Optional 객체
     */
    public Optional<SeoulJobInfo> getJobInfo(int startIndex, int endIndex) {

        String path = API_PATH
                .replace("{API_KEY}", API_KEY)
                .replace("{start}", String.valueOf(startIndex))
                .replace("{end}", String.valueOf(endIndex));

        // WebClient를 사용한 동기/블로킹 방식 호출 (RestTemplate 대체)
        Mono<String> xmlResponseMono = webClient.get()
                .uri(path)
                .retrieve()
                // HTTP 상태 코드에 따른 명시적인 에러 처리
                .onStatus(httpStatus -> httpStatus.is4xxClientError() || httpStatus.is5xxServerError(),
                        clientResponse -> {
                            // 에러 응답 코드를 확인합니다.
                            String errorMessage = String.format("API 서버 응답 오류: HTTP %s", clientResponse.statusCode());
                            System.err.println(errorMessage);
                            // APIClientException을 던져 상위 계층에서 잡도록 합니다.
                            return Mono.error(
                                    new ApiClientException(ErrorCode.API_EXTERNAL_COMMUNICATION_ERROR, errorMessage,
                                            null));
                        })
                .bodyToMono(String.class)
                // 재시도 로직 추가: 3번까지 재시도
                .retryWhen(reactor.util.retry.Retry.max(3));

        try {
            // Mono를 블로킹 방식으로 기다려 결과를 가져옴 (배치 처리 시 일반적)
            String xmlResponse = xmlResponseMono.block();

            // 받은 XML 전문을 로그로 출력하여 구조를 확인
            //log.error("--- API 응답 XML 전문 --- \n {}", xmlResponse);

            if (xmlResponse == null || xmlResponse.isEmpty()) {
                System.err.println("API 응답 XML이 NULL이거나 비어있습니다. API 키 또는 URL 확인 필요.");
                return Optional.empty();
            }

            // JAXB 패키지를 jakarta.xml.bind로 변경
            JAXBContext jaxbContext = JAXBContext.newInstance(SeoulJobInfo.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = new StringReader(xmlResponse);

            return Optional.of((SeoulJobInfo) unmarshaller.unmarshal(reader));

        } catch (JAXBException e) {
            // JAXB 파싱 오류 발생 시 전역 ErrorCode 사용
            throw new ApiClientException(ErrorCode.API_PARSING_ERROR, "서울일자리 XML 응답 파싱 실패", e);
        } catch (Exception e) {
            // 네트워크 등 기타 오류 발생 시 전역 ErrorCode 사용
            throw new ApiClientException(ErrorCode.API_EXTERNAL_COMMUNICATION_ERROR, "서울일자리 API 호출 중 통신 오류 발생", e);
        }
    }
}