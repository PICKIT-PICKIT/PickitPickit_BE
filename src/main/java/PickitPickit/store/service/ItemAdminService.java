package PickitPickit.store.service;

import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import PickitPickit.store.domain.Item;
import PickitPickit.store.domain.ItemTag;
import PickitPickit.store.domain.Tag;
import PickitPickit.store.dto.ItemCreateRequest;
import PickitPickit.store.dto.ItemResponse;
import PickitPickit.store.dto.ItemUpdateRequest;
import PickitPickit.store.dto.TagResponse;
import PickitPickit.store.repository.ItemRepository;
import PickitPickit.store.repository.ItemTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemAdminService {

    private final ItemRepository itemRepository;
    private final ItemTagRepository itemTagRepository;
    private final TagServiceSupport tagServiceSupport;

    @Transactional
    public ItemResponse create(ItemCreateRequest request) {
        if (itemRepository.existsByName(request.name().trim())) {
            throw new ApiException(ErrorStatus.DUPLICATE_RESOURCE, "이미 등록된 상품명입니다.");
        }

        Item item = itemRepository.save(Item.create(request.name(), request.category(), request.defaultImageUrl()));
        replaceItemTags(item, request.tags());
        return ItemResponse.from(item, getItemTags(item.getId()));
    }

    public List<ItemResponse> getItems() {
        List<Item> items = itemRepository.findAllByOrderByNameAsc();
        List<Long> itemIds = items.stream().map(Item::getId).toList();
        Map<Long, List<TagResponse>> tagMap = getItemTagMap(itemIds);

        return items.stream()
                .map(item -> ItemResponse.from(item, tagMap.getOrDefault(item.getId(), List.of())))
                .toList();
    }

    @Transactional
    public ItemResponse update(Long itemId, ItemUpdateRequest request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));

        item.update(request.name(), request.category(), request.defaultImageUrl());
        if (request.tags() != null) {
            replaceItemTags(item, request.tags());
        }

        return ItemResponse.from(item, getItemTags(item.getId()));
    }

    private void replaceItemTags(Item item, List<String> tagNames) {
        itemTagRepository.deleteAllByItemId(item.getId());

        List<Tag> tags = tagServiceSupport.getOrCreateAll(tagNames);
        List<ItemTag> itemTags = tags.stream()
                .map(tag -> ItemTag.create(item, tag))
                .toList();

        itemTagRepository.saveAll(itemTags);
    }

    private List<TagResponse> getItemTags(Long itemId) {
        return itemTagRepository.findAllByItemIdOrderByTagNameAsc(itemId)
                .stream()
                .map(ItemTag::getTag)
                .map(TagResponse::from)
                .toList();
    }

    private Map<Long, List<TagResponse>> getItemTagMap(Collection<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Map.of();
        }

        return itemTagRepository.findAllByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(
                        itemTag -> itemTag.getItem().getId(),
                        Collectors.mapping(itemTag -> TagResponse.from(itemTag.getTag()), Collectors.toList())
                ));
    }
}
