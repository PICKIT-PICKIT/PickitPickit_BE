package PickitPickit.store.client;

import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ClaudeRecommendationClient {

    private static final String MESSAGES_PATH = "/v1/messages";
    private static final int MAX_OUTPUT_TOKENS = 512;
    private static final double TEMPERATURE = 0.2;
    private static final String SYSTEM_PROMPT = """
            당신은 PickitPickit의 오프라인 매장 추천 엔진입니다.
            사용자의 관심 태그, 검색어, 현재 위치, 후보 매장 데이터를 보고 추천 매장을 고릅니다.
            반드시 후보 매장 목록에 있는 storeId만 사용하세요.
            응답은 설명 문장 없이 JSON 객체만 반환하세요.
            형식: {"recommendations":[{"storeId":1,"reason":"추천 이유"}]}
            """;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient;
    private final String apiKey;
    private final String baseUrl;
    private final String apiVersion;
    private final String model;

    public ClaudeRecommendationClient(
            @Value("${claude.api.key:}") String apiKey,
            @Value("${claude.api.base-url:https://api.anthropic.com}") String baseUrl,
            @Value("${claude.api.version:2023-06-01}") String apiVersion,
            @Value("${claude.api.model:claude-sonnet-4-6}") String model
    ) {
        this.restClient = RestClient.create();
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.apiVersion = apiVersion;
        this.model = model;
    }

    public List<ClaudeStoreRecommendationResult> recommendStores(String prompt) {
        if (!StringUtils.hasText(apiKey)) {
            throw new ApiException(ErrorStatus.EXTERNAL_ERROR, "Claude API key가 설정되지 않았습니다.");
        }

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", MAX_OUTPUT_TOKENS,
                "temperature", TEMPERATURE,
                "system", SYSTEM_PROMPT,
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", prompt
                ))
        );

        try {
            ClaudeMessageResponse response = restClient.post()
                    .uri(baseUrl + MESSAGES_PATH)
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", apiVersion)
                    .header("content-type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(ClaudeMessageResponse.class);

            return parseRecommendations(response);
        } catch (RestClientException e) {
            log.warn("Claude 추천 API 호출 실패: {}", e.getMessage());
            throw new ApiException(ErrorStatus.EXTERNAL_ERROR, "Claude 추천 API 호출에 실패했습니다.");
        }
    }

    private List<ClaudeStoreRecommendationResult> parseRecommendations(ClaudeMessageResponse response) {
        String text = extractText(response);
        if (!StringUtils.hasText(text)) {
            return List.of();
        }

        try {
            ClaudeRecommendationPayload payload = objectMapper.readValue(text, ClaudeRecommendationPayload.class);
            if (payload.recommendations() == null) {
                return List.of();
            }
            return payload.recommendations();
        } catch (JsonProcessingException e) {
            log.warn("Claude 추천 응답 파싱 실패: {}", e.getMessage());
            throw new ApiException(ErrorStatus.EXTERNAL_ERROR, "Claude 추천 응답을 파싱할 수 없습니다.");
        }
    }

    private String extractText(ClaudeMessageResponse response) {
        if (response == null || response.content() == null) {
            return null;
        }

        return response.content()
                .stream()
                .filter(content -> "text".equals(content.type()))
                .map(ClaudeContentBlock::text)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }

    private record ClaudeMessageResponse(
            List<ClaudeContentBlock> content
    ) {
    }

    private record ClaudeContentBlock(
            String type,
            String text
    ) {
    }

    private record ClaudeRecommendationPayload(
            List<ClaudeStoreRecommendationResult> recommendations
    ) {
    }
}
