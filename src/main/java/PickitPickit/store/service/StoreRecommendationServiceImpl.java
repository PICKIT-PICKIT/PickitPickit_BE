package PickitPickit.store.service;

import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import PickitPickit.onboarding.domain.UserInterestTag;
import PickitPickit.onboarding.repository.UserInterestTagRepository;
import PickitPickit.store.client.ClaudeRecommendationClient;
import PickitPickit.store.client.ClaudeStoreRecommendationResult;
import PickitPickit.store.domain.StoreProduct;
import PickitPickit.store.domain.StoreTag;
import PickitPickit.store.dto.StoreRecommendationResponse;
import PickitPickit.store.dto.StoreResponse;
import PickitPickit.store.dto.StoreType;
import PickitPickit.store.repository.StoreProductRepository;
import PickitPickit.store.repository.StoreTagRepository;
import PickitPickit.user.domain.UserStatus;
import PickitPickit.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreRecommendationServiceImpl implements StoreRecommendationService {

    private static final int DEFAULT_RECOMMENDATION_RADIUS_METERS = 3000;
    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 10;
    private static final int DEFAULT_SEARCH_LIMIT = 50;
    private static final int MAX_CANDIDATE_COUNT = 20;
    private static final int MAX_PRODUCTS_PER_STORE = 5;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserRepository userRepository;
    private final UserInterestTagRepository userInterestTagRepository;
    private final StoreService storeService;
    private final StoreTagRepository storeTagRepository;
    private final StoreProductRepository storeProductRepository;
    private final ClaudeRecommendationClient claudeRecommendationClient;

    @Override
    public List<StoreRecommendationResponse> recommendStores(
            Long userId,
            String keyword,
            double latitude,
            double longitude,
            StoreType type,
            int limit
    ) {
        validateActiveUser(userId);
        String normalizedKeyword = normalizeKeyword(keyword);
        validateCoordinate(latitude, longitude);
        validateLimit(limit);

        StoreType safeType = type == null ? StoreType.ALL : type;
        List<StoreResponse> candidateStores = findCandidateStores(
                normalizedKeyword,
                latitude,
                longitude,
                safeType
        );

        if (candidateStores.isEmpty()) {
            return List.of();
        }

        List<String> interestTags = getInterestTags(userId);
        List<RecommendationCandidate> candidates = enrichCandidates(candidateStores);
        Map<Long, RecommendationCandidate> candidateMap = candidates.stream()
                .collect(Collectors.toMap(
                        candidate -> candidate.store().getId(),
                        candidate -> candidate,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        List<StoreRecommendationResponse> recommendations = new ArrayList<>();
        try {
            String prompt = buildPrompt(normalizedKeyword, latitude, longitude, limit, interestTags, candidates);
            recommendations.addAll(applyClaudeRecommendations(
                    claudeRecommendationClient.recommendStores(prompt),
                    candidateMap,
                    limit
            ));
        } catch (RuntimeException e) {
            log.warn("Claude 매장 추천 실패. 거리순 fallback을 사용합니다: {}", e.getMessage());
        }

        fillFallbackRecommendations(recommendations, candidates, normalizedKeyword, interestTags, limit);
        return recommendations;
    }

    private List<StoreResponse> findCandidateStores(
            String keyword,
            double latitude,
            double longitude,
            StoreType type
    ) {
        LinkedHashMap<Long, StoreResponse> candidates = new LinkedHashMap<>();

        storeService.searchStores(keyword, type, latitude, longitude, DEFAULT_SEARCH_LIMIT)
                .stream()
                .filter(store -> store.getDistance() <= DEFAULT_RECOMMENDATION_RADIUS_METERS)
                .forEach(store -> candidates.putIfAbsent(store.getId(), store));

        storeService.getNearbyStores(latitude, longitude, DEFAULT_RECOMMENDATION_RADIUS_METERS, type)
                .forEach(store -> candidates.putIfAbsent(store.getId(), store));

        return candidates.values()
                .stream()
                .limit(MAX_CANDIDATE_COUNT)
                .toList();
    }

    private List<RecommendationCandidate> enrichCandidates(List<StoreResponse> stores) {
        List<Long> storeIds = stores.stream()
                .map(StoreResponse::getId)
                .toList();

        Map<Long, List<String>> storeTagMap = getStoreTagMap(storeIds);
        Map<Long, List<String>> productNameMap = getProductNameMap(storeIds);

        return stores.stream()
                .map(store -> new RecommendationCandidate(
                        store,
                        storeTagMap.getOrDefault(store.getId(), List.of()),
                        productNameMap.getOrDefault(store.getId(), List.of())
                ))
                .toList();
    }

    private Map<Long, List<String>> getStoreTagMap(List<Long> storeIds) {
        if (storeIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, LinkedHashSet<String>> tagMap = new LinkedHashMap<>();
        for (StoreTag storeTag : storeTagRepository.findAllByStoreIdInOrderByTagNameAsc(storeIds)) {
            tagMap.computeIfAbsent(storeTag.getStore().getId(), ignored -> new LinkedHashSet<>())
                    .add(storeTag.getTag().getName());
        }

        return toListMap(tagMap);
    }

    private Map<Long, List<String>> getProductNameMap(List<Long> storeIds) {
        if (storeIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, LinkedHashSet<String>> productMap = new LinkedHashMap<>();
        for (StoreProduct storeProduct : storeProductRepository.findAllByStoreIdIn(storeIds)) {
            LinkedHashSet<String> productNames =
                    productMap.computeIfAbsent(storeProduct.getStore().getId(), ignored -> new LinkedHashSet<>());

            if (productNames.size() < MAX_PRODUCTS_PER_STORE) {
                productNames.add(storeProduct.getItem().getName());
            }
        }

        return toListMap(productMap);
    }

    private Map<Long, List<String>> toListMap(Map<Long, LinkedHashSet<String>> source) {
        Map<Long, List<String>> result = new LinkedHashMap<>();
        source.forEach((storeId, values) -> result.put(storeId, values.stream().toList()));
        return result;
    }

    private List<String> getInterestTags(Long userId) {
        return userInterestTagRepository.findByUserId(userId)
                .stream()
                .map(UserInterestTag::getInterestTag)
                .map(interestTag -> interestTag.getName())
                .toList();
    }

    private String buildPrompt(
            String keyword,
            double latitude,
            double longitude,
            int limit,
            List<String> interestTags,
            List<RecommendationCandidate> candidates
    ) {
        List<Map<String, Object>> candidateData = candidates.stream()
                .map(candidate -> Map.<String, Object>of(
                        "storeId", candidate.store().getId(),
                        "name", candidate.store().getName(),
                        "type", candidate.store().getType().name(),
                        "address", candidate.store().getAddress(),
                        "distanceMeters", candidate.store().getDistance(),
                        "storeTags", candidate.storeTags(),
                        "products", candidate.productNames()
                ))
                .toList();

        Map<String, Object> promptData = Map.of(
                "interestTags", interestTags,
                "keyword", keyword,
                "location", Map.of(
                        "latitude", latitude,
                        "longitude", longitude
                ),
                "limit", limit,
                "candidateStores", candidateData
        );

        try {
            return """
                    아래 JSON 데이터를 기준으로 사용자에게 추천할 매장을 최대 %d개 골라주세요.
                    추천 기준은 검색어와의 관련성, 사용자 관심 태그와의 관련성,
                    현재 위치와의 거리입니다.
                    반드시 candidateStores 안에 있는 storeId만 선택하세요.
                    reason은 한국어 한 문장으로 간결하게 작성하세요.
                    응답은 {"recommendations":[{"storeId":1,"reason":"추천 이유"}]} 형식의 JSON만 반환하세요.

                    %s
                    """.formatted(limit, objectMapper.writeValueAsString(promptData));
        } catch (JsonProcessingException e) {
            throw new ApiException(ErrorStatus.INTERNAL_SERVER_ERROR, "추천 프롬프트를 생성할 수 없습니다.");
        }
    }

    private List<StoreRecommendationResponse> applyClaudeRecommendations(
            List<ClaudeStoreRecommendationResult> claudeResults,
            Map<Long, RecommendationCandidate> candidateMap,
            int limit
    ) {
        if (claudeResults == null || claudeResults.isEmpty()) {
            return List.of();
        }

        List<StoreRecommendationResponse> recommendations = new ArrayList<>();
        Set<Long> selectedStoreIds = new LinkedHashSet<>();

        for (ClaudeStoreRecommendationResult result : claudeResults) {
            if (recommendations.size() >= limit || result == null || result.storeId() == null) {
                continue;
            }

            RecommendationCandidate candidate = candidateMap.get(result.storeId());
            if (candidate == null || selectedStoreIds.contains(result.storeId())) {
                continue;
            }

            selectedStoreIds.add(result.storeId());
            recommendations.add(new StoreRecommendationResponse(
                    candidate.store(),
                    normalizeReason(result.reason())
            ));
        }

        return recommendations;
    }

    private void fillFallbackRecommendations(
            List<StoreRecommendationResponse> recommendations,
            List<RecommendationCandidate> candidates,
            String keyword,
            List<String> interestTags,
            int limit
    ) {
        Set<Long> selectedStoreIds = recommendations.stream()
                .map(response -> response.store().getId())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (RecommendationCandidate candidate : candidates) {
            if (recommendations.size() >= limit) {
                return;
            }
            if (selectedStoreIds.contains(candidate.store().getId())) {
                continue;
            }

            selectedStoreIds.add(candidate.store().getId());
            recommendations.add(new StoreRecommendationResponse(
                    candidate.store(),
                    fallbackReason(keyword, interestTags)
            ));
        }
    }

    private String normalizeReason(String reason) {
        if (!StringUtils.hasText(reason)) {
            return "검색어, 관심 태그, 현재 위치를 기준으로 추천한 매장입니다.";
        }
        return reason.trim();
    }

    private String fallbackReason(String keyword, List<String> interestTags) {
        if (!interestTags.isEmpty()) {
            return "검색어 '" + keyword + "'와 관심 태그, 현재 위치를 기준으로 가까운 매장입니다.";
        }
        return "검색어 '" + keyword + "'와 현재 위치를 기준으로 가까운 매장입니다.";
    }

    private void validateActiveUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "userId는 필수이며 1 이상이어야 합니다.");
        }

        userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(
                        ErrorStatus.USER_NOT_FOUND,
                        "해당 사용자를 찾을 수 없습니다."
                ));
    }

    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "검색어(keyword)는 필수입니다.");
        }

        String normalized = keyword.trim();
        if (normalized.length() > 255) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "검색어(keyword)는 255자 이하여야 합니다.");
        }
        return normalized;
    }

    private void validateCoordinate(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "위도(lat)는 -90 이상 90 이하여야 합니다.");
        }
        if (longitude < -180 || longitude > 180) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "경도(lng)는 -180 이상 180 이하여야 합니다.");
        }
    }

    private void validateLimit(int limit) {
        if (limit < MIN_LIMIT || limit > MAX_LIMIT) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "limit은 1 이상 10 이하여야 합니다.");
        }
    }

    private record RecommendationCandidate(
            StoreResponse store,
            List<String> storeTags,
            List<String> productNames
    ) {
    }
}
