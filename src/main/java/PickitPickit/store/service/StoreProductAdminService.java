package PickitPickit.store.service;

import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import PickitPickit.store.domain.*;
import PickitPickit.store.dto.StoreProductCreateRequest;
import PickitPickit.store.dto.StoreProductResponse;
import PickitPickit.store.dto.StoreProductUpdateRequest;
import PickitPickit.store.dto.TagResponse;
import PickitPickit.store.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreProductAdminService {

    private final StoreRepository storeRepository;
    private final ItemRepository itemRepository;
    private final StoreProductRepository storeProductRepository;
    private final StoreProductTagRepository storeProductTagRepository;
    private final ItemTagRepository itemTagRepository;
    private final TagServiceSupport tagServiceSupport;
    private final StoreTagSyncService storeTagSyncService;

    @Transactional
    public StoreProductResponse create(StoreProductCreateRequest request) {
        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND, "매장을 찾을 수 없습니다."));
        Item item = itemRepository.findById(request.itemId())
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));

        if (storeProductRepository.existsByStoreIdAndItemId(store.getId(), item.getId())) {
            throw new ApiException(ErrorStatus.DUPLICATE_RESOURCE, "이미 해당 매장에 등록된 상품입니다.");
        }

        StoreProduct storeProduct = StoreProduct.create(
                store,
                item,
                request.price(),
                request.inventoryMode(),
                request.stockQuantity(),
                request.stockStatus(),
                request.difficulty(),
                request.imageUrl()
        );

        StoreProduct saved = storeProductRepository.save(storeProduct);
        List<Tag> tags = replaceProductTags(saved, resolveTagNames(item, request.tags()));
        storeTagSyncService.syncAutoTagsAfterProductTagChange(store, tags);

        return StoreProductResponse.from(saved, getProductTags(saved.getId()));
    }

    @Transactional
    public StoreProductResponse update(Long storeProductId, StoreProductUpdateRequest request) {
        StoreProduct storeProduct = storeProductRepository.findById(storeProductId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND, "매장 상품을 찾을 수 없습니다."));

        storeProduct.update(
                request.price(),
                request.inventoryMode(),
                request.stockQuantity(),
                request.stockStatus(),
                request.difficulty(),
                request.imageUrl()
        );

        if (request.tags() != null) {
            List<Tag> tags = replaceProductTags(storeProduct, resolveTagNames(storeProduct.getItem(), request.tags()));
            storeTagSyncService.syncAutoTagsAfterProductTagChange(storeProduct.getStore(), tags);
        }

        return StoreProductResponse.from(storeProduct, getProductTags(storeProduct.getId()));
    }

    @Transactional
    public void delete(Long storeProductId) {
        StoreProduct storeProduct = storeProductRepository.findById(storeProductId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND, "매장 상품을 찾을 수 없습니다."));

        Long storeId = storeProduct.getStore().getId();

        storeProductTagRepository.deleteAllByStoreProductId(storeProductId);
        storeProductRepository.delete(storeProduct);
        storeProductRepository.flush();
        storeTagSyncService.cleanupUnusedAutoTags(storeId);
    }

    private List<String> resolveTagNames(Item item, List<String> requestedTagNames) {
        if (requestedTagNames != null) {
            return requestedTagNames;
        }

        return itemTagRepository.findAllByItemIdOrderByTagNameAsc(item.getId())
                .stream()
                .map(itemTag -> itemTag.getTag().getName())
                .toList();
    }

    private List<Tag> replaceProductTags(StoreProduct storeProduct, List<String> tagNames) {
        storeProductTagRepository.deleteAllByStoreProductId(storeProduct.getId());

        List<Tag> tags = tagServiceSupport.getOrCreateAll(tagNames);
        List<StoreProductTag> productTags = tags.stream()
                .map(tag -> StoreProductTag.create(storeProduct, tag))
                .toList();

        storeProductTagRepository.saveAll(productTags);
        return tags;
    }

    private List<TagResponse> getProductTags(Long storeProductId) {
        return storeProductTagRepository.findAllByStoreProductIdOrderByTagNameAsc(storeProductId)
                .stream()
                .map(StoreProductTag::getTag)
                .map(TagResponse::from)
                .toList();
    }
}
