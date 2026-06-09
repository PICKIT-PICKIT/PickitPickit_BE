package PickitPickit.store.service;

import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import PickitPickit.onboarding.domain.InterestTag;
import PickitPickit.onboarding.domain.UserInterestTag;
import PickitPickit.onboarding.repository.UserInterestTagRepository;
import PickitPickit.store.client.ClaudeRecommendationClient;
import PickitPickit.store.client.ClaudeStoreRecommendationResult;
import PickitPickit.store.domain.InventoryMode;
import PickitPickit.store.domain.Item;
import PickitPickit.store.domain.MapSourceType;
import PickitPickit.store.domain.ProductCategory;
import PickitPickit.store.domain.StockStatus;
import PickitPickit.store.domain.Store;
import PickitPickit.store.domain.StoreProduct;
import PickitPickit.store.domain.StoreTag;
import PickitPickit.store.domain.StoreTagSource;
import PickitPickit.store.domain.Tag;
import PickitPickit.store.dto.StoreRecommendationResponse;
import PickitPickit.store.dto.StoreResponse;
import PickitPickit.store.dto.StoreType;
import PickitPickit.store.repository.StoreProductRepository;
import PickitPickit.store.repository.StoreTagRepository;
import PickitPickit.user.domain.User;
import PickitPickit.user.domain.UserStatus;
import PickitPickit.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreRecommendationServiceTest {

    private static final double LATITUDE = 37.5665;
    private static final double LONGITUDE = 126.9780;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserInterestTagRepository userInterestTagRepository;

    @Mock
    private StoreService storeService;

    @Mock
    private StoreTagRepository storeTagRepository;

    @Mock
    private StoreProductRepository storeProductRepository;

    @Mock
    private ClaudeRecommendationClient claudeRecommendationClient;

    private StoreRecommendationServiceImpl storeRecommendationService;

    @BeforeEach
    void setUp() {
        storeRecommendationService = new StoreRecommendationServiceImpl(
                userRepository,
                userInterestTagRepository,
                storeService,
                storeTagRepository,
                storeProductRepository,
                claudeRecommendationClient
        );
    }

    @Test
    void recommendStoresUsesClaudeOrderAndPromptVariables() {
        User user = user();
        Store pikachuStore = store(1L, "피카츄 뽑기", StoreType.CLAW, LATITUDE, LONGITUDE);
        Store gachaStore = store(2L, "홍대 가챠존", StoreType.GACHA, 37.5670, 126.9780);
        Store figureStore = store(3L, "피규어 라운지", StoreType.CLAW, 37.5680, 126.9780);

        when(userRepository.findByIdAndStatus(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(userInterestTagRepository.findByUserId(1L))
                .thenReturn(List.of(UserInterestTag.create(user, interestTag(10L, "포켓몬"))));
        when(storeService.searchStores("포켓몬", StoreType.ALL, LATITUDE, LONGITUDE, 50))
                .thenReturn(List.of(StoreResponse.from(gachaStore, 450)));
        when(storeService.getNearbyStores(LATITUDE, LONGITUDE, 3000, StoreType.ALL))
                .thenReturn(List.of(
                        StoreResponse.from(pikachuStore, 200),
                        StoreResponse.from(gachaStore, 450),
                        StoreResponse.from(figureStore, 700)
                ));
        when(storeTagRepository.findAllByStoreIdInOrderByTagNameAsc(List.of(2L, 1L, 3L)))
                .thenReturn(List.of(
                        StoreTag.create(gachaStore, tag("가챠"), StoreTagSource.MANUAL),
                        StoreTag.create(pikachuStore, tag("포켓몬"), StoreTagSource.MANUAL)
                ));
        when(storeProductRepository.findAllByStoreIdIn(List.of(2L, 1L, 3L)))
                .thenReturn(List.of(
                        storeProduct(gachaStore, "포켓몬 키링"),
                        storeProduct(pikachuStore, "피카츄 인형"),
                        storeProduct(figureStore, "애니 피규어")
                ));
        when(claudeRecommendationClient.recommendStores(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(List.of(
                        new ClaudeStoreRecommendationResult(1L, "관심 태그인 포켓몬과 상품 구성이 잘 맞습니다."),
                        new ClaudeStoreRecommendationResult(2L, "검색어와 가까운 가챠 매장입니다."),
                        new ClaudeStoreRecommendationResult(3L, "가까운 거리의 피규어 매장입니다.")
                ));

        List<StoreRecommendationResponse> responses = storeRecommendationService.recommendStores(
                1L,
                " 포켓몬 ",
                LATITUDE,
                LONGITUDE,
                StoreType.ALL,
                3
        );

        assertThat(responses)
                .extracting(response -> response.store().getId())
                .containsExactly(1L, 2L, 3L);
        assertThat(responses)
                .extracting(StoreRecommendationResponse::recommendationReason)
                .containsExactly(
                        "관심 태그인 포켓몬과 상품 구성이 잘 맞습니다.",
                        "검색어와 가까운 가챠 매장입니다.",
                        "가까운 거리의 피규어 매장입니다."
                );

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(claudeRecommendationClient).recommendStores(promptCaptor.capture());
        assertThat(promptCaptor.getValue())
                .contains("포켓몬")
                .contains("37.5665")
                .contains("126.978")
                .contains("피카츄 뽑기")
                .contains("포켓몬 키링");
    }

    @Test
    void recommendStoresFallsBackWhenClaudeFails() {
        User user = user();
        Store firstStore = store(1L, "가까운 매장", StoreType.CLAW, LATITUDE, LONGITUDE);
        Store secondStore = store(2L, "두번째 매장", StoreType.GACHA, 37.5670, 126.9780);
        Store thirdStore = store(3L, "세번째 매장", StoreType.CLAW, 37.5680, 126.9780);

        when(userRepository.findByIdAndStatus(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(userInterestTagRepository.findByUserId(1L))
                .thenReturn(List.of(UserInterestTag.create(user, interestTag(10L, "피규어"))));
        when(storeService.searchStores("피규어", StoreType.ALL, LATITUDE, LONGITUDE, 50))
                .thenReturn(List.of());
        when(storeService.getNearbyStores(LATITUDE, LONGITUDE, 3000, StoreType.ALL))
                .thenReturn(List.of(
                        StoreResponse.from(firstStore, 100),
                        StoreResponse.from(secondStore, 300),
                        StoreResponse.from(thirdStore, 500)
                ));
        when(storeTagRepository.findAllByStoreIdInOrderByTagNameAsc(List.of(1L, 2L, 3L)))
                .thenReturn(List.of());
        when(storeProductRepository.findAllByStoreIdIn(List.of(1L, 2L, 3L)))
                .thenReturn(List.of());
        when(claudeRecommendationClient.recommendStores(org.mockito.ArgumentMatchers.anyString()))
                .thenThrow(new ApiException(ErrorStatus.EXTERNAL_ERROR));

        List<StoreRecommendationResponse> responses = storeRecommendationService.recommendStores(
                1L,
                "피규어",
                LATITUDE,
                LONGITUDE,
                StoreType.ALL,
                3
        );

        assertThat(responses)
                .extracting(response -> response.store().getId())
                .containsExactly(1L, 2L, 3L);
        assertThat(responses)
                .extracting(StoreRecommendationResponse::recommendationReason)
                .allSatisfy(reason -> assertThat(reason).contains("피규어"));
    }

    @Test
    void recommendStoresReturnsEmptyWithoutCallingClaudeWhenNoCandidatesExist() {
        User user = user();
        when(userRepository.findByIdAndStatus(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(storeService.searchStores("없는상품", StoreType.ALL, LATITUDE, LONGITUDE, 50))
                .thenReturn(List.of());
        when(storeService.getNearbyStores(LATITUDE, LONGITUDE, 3000, StoreType.ALL))
                .thenReturn(List.of());

        List<StoreRecommendationResponse> responses = storeRecommendationService.recommendStores(
                1L,
                "없는상품",
                LATITUDE,
                LONGITUDE,
                StoreType.ALL,
                3
        );

        assertThat(responses).isEmpty();
        verify(userInterestTagRepository, never()).findByUserId(1L);
        verify(storeTagRepository, never()).findAllByStoreIdInOrderByTagNameAsc(anyList());
        verify(storeProductRepository, never()).findAllByStoreIdIn(anyList());
        verify(claudeRecommendationClient, never()).recommendStores(org.mockito.ArgumentMatchers.anyString());
    }

    private User user() {
        User user = User.createFromKakao("12345", "민수", null);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private Store store(Long id, String name, StoreType type, double latitude, double longitude) {
        Store store = Store.builder()
                .sourceType(MapSourceType.MANUAL)
                .sourcePlaceId("manual-" + id)
                .name(name)
                .storeType(type)
                .address("서울시 테스트구 테스트로 " + id)
                .latitude(BigDecimal.valueOf(latitude))
                .longitude(BigDecimal.valueOf(longitude))
                .build();
        ReflectionTestUtils.setField(store, "id", id);
        return store;
    }

    private InterestTag interestTag(Long id, String name) {
        try {
            var constructor = InterestTag.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            InterestTag interestTag = constructor.newInstance();
            ReflectionTestUtils.setField(interestTag, "id", id);
            ReflectionTestUtils.setField(interestTag, "name", name);
            ReflectionTestUtils.setField(interestTag, "displayOrder", id.intValue());
            ReflectionTestUtils.setField(interestTag, "active", true);
            return interestTag;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private Tag tag(String name) {
        return Tag.create(name);
    }

    private StoreProduct storeProduct(Store store, String itemName) {
        return StoreProduct.create(
                store,
                Item.create(itemName, ProductCategory.FIGURE, null),
                1000,
                InventoryMode.STATUS,
                null,
                StockStatus.IN_STOCK,
                3,
                null
        );
    }
}
