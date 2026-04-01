package PickitPickit.store.client;

import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * 카카오 로컬 키워드 검색 API 클라이언트
 * 문서: https://developers.kakao.com/docs/latest/ko/local/dev-guide#search-by-keyword
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoLocalClient {

    private static final String KEYWORD_SEARCH_PATH = "/v2/local/search/keyword.json";

    @Value("${kakao.api.key}")
    private String apiKey;

    @Value("${kakao.api.base-url}")
    private String baseUrl;

    /**
     * 키워드, 위치, 반경으로 장소 검색
     *
     * @param keyword 검색 키워드
     * @param latitude 기준 위도
     * @param longitude 기준 경도
     * @param radiusMeters 검색 반경 (m)
     * @param page 페이지 번호 (1~45)
     * @return 카카오 키워드 검색 응답
     */
    public KakaoKeywordResponse searchByKeyword(String keyword,
                                                double latitude,
                                                double longitude,
                                                int radiusMeters,
                                                int page) {
        URI uri = UriComponentsBuilder
                .fromUriString(baseUrl + KEYWORD_SEARCH_PATH)
                .queryParam("query", keyword)
                .queryParam("y", latitude)   // 위도
                .queryParam("x", longitude)  // 경도
                .queryParam("radius", radiusMeters)
                .queryParam("sort", "distance")
                .queryParam("page", page)
                .queryParam("size", 15)
                .build()
                .encode()
                .toUri();

        try {
            return RestClient.create()
                    .get()
                    .uri(uri)
                    .header("Authorization", "KakaoAK " + apiKey)
                    .retrieve()
                    .body(KakaoKeywordResponse.class);
        } catch (RestClientException e) {
            log.error("카카오 로컬 API 호출 실패: {}", e.getMessage());
            throw new ApiException(ErrorStatus.KAKAO_API_ERROR);
        }
    }
}
