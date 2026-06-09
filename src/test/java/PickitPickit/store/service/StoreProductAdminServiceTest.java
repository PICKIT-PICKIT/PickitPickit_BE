package PickitPickit.store.service;

import PickitPickit.store.domain.InventoryMode;
import PickitPickit.store.domain.Item;
import PickitPickit.store.domain.MapSourceType;
import PickitPickit.store.domain.ProductCategory;
import PickitPickit.store.domain.StockStatus;
import PickitPickit.store.domain.Store;
import PickitPickit.store.domain.StoreProduct;
import PickitPickit.store.domain.StoreProductTag;
import PickitPickit.store.domain.StoreTag;
import PickitPickit.store.domain.StoreTagSource;
import PickitPickit.store.domain.Tag;
import PickitPickit.store.dto.StoreProductResponse;
import PickitPickit.store.dto.StoreProductUpdateRequest;
import PickitPickit.store.dto.StoreType;
import PickitPickit.store.repository.ItemRepository;
import PickitPickit.store.repository.StoreProductRepository;
import PickitPickit.store.repository.StoreProductTagRepository;
import PickitPickit.store.repository.StoreRepository;
import PickitPickit.store.repository.StoreTagRepository;
import PickitPickit.store.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class StoreProductAdminServiceTest {

    @Autowired
    private StoreProductAdminService storeProductAdminService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private StoreProductRepository storeProductRepository;

    @Autowired
    private StoreProductTagRepository storeProductTagRepository;

    @Autowired
    private StoreTagRepository storeTagRepository;

    @Autowired
    private TagRepository tagRepository;

    @Test
    void updateReplacesTagsWithExistingNewAndDuplicateNames() {
        Store store = storeRepository.save(Store.builder()
                .sourceType(MapSourceType.MANUAL)
                .sourcePlaceId("manual-" + UUID.randomUUID())
                .name("테스트 매장")
                .storeType(StoreType.CLAW)
                .address("경기도 테스트시 테스트로 1")
                .latitude(BigDecimal.valueOf(37.12345678))
                .longitude(BigDecimal.valueOf(127.12345678))
                .build());
        Item item = itemRepository.save(Item.create("원피스 피규어", ProductCategory.FIGURE, "https://example.com/item.png"));
        StoreProduct storeProduct = storeProductRepository.save(StoreProduct.create(
                store,
                item,
                1000,
                InventoryMode.STATUS,
                null,
                StockStatus.IN_STOCK,
                3,
                null
        ));
        Tag originalTag = tagRepository.save(Tag.create("원피스"));
        storeProductTagRepository.save(StoreProductTag.create(storeProduct, originalTag));
        storeTagRepository.save(StoreTag.create(store, originalTag, StoreTagSource.AUTO));

        StoreProductUpdateRequest request = new StoreProductUpdateRequest(
                1500,
                InventoryMode.STATUS,
                null,
                StockStatus.IN_STOCK,
                4,
                null,
                List.of("원피스", "루피", "루피")
        );

        StoreProductResponse response = storeProductAdminService.update(storeProduct.getId(), request);

        assertThat(response.itemName()).isEqualTo("원피스 피규어");
        assertThat(response.price()).isEqualTo(1500);
        assertThat(response.difficulty()).isEqualTo(4);
        assertThat(response.tags())
                .extracting("name")
                .containsExactly("루피", "원피스");
        assertThat(storeProductTagRepository.findAllByStoreProductIdOrderByTagNameAsc(storeProduct.getId()))
                .extracting(storeProductTag -> storeProductTag.getTag().getName())
                .containsExactly("루피", "원피스");
    }
}
