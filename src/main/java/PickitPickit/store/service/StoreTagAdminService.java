package PickitPickit.store.service;

import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import PickitPickit.store.domain.Store;
import PickitPickit.store.domain.StoreTag;
import PickitPickit.store.domain.StoreTagSource;
import PickitPickit.store.domain.Tag;
import PickitPickit.store.dto.StoreTagReplaceRequest;
import PickitPickit.store.dto.TagResponse;
import PickitPickit.store.repository.StoreProductTagRepository;
import PickitPickit.store.repository.StoreRepository;
import PickitPickit.store.repository.StoreTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreTagAdminService {

    private final StoreRepository storeRepository;
    private final StoreTagRepository storeTagRepository;
    private final StoreProductTagRepository storeProductTagRepository;
    private final TagServiceSupport tagServiceSupport;

    public List<TagResponse> getStoreTags(Long storeId) {
        validateStoreExists(storeId);
        return storeTagRepository.findAllByStoreIdOrderByTagNameAsc(storeId)
                .stream()
                .map(StoreTag::getTag)
                .map(TagResponse::from)
                .toList();
    }

    @Transactional
    public List<TagResponse> replaceStoreTags(Long storeId, StoreTagReplaceRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND, "매장을 찾을 수 없습니다."));

        List<Tag> requestedTags = tagServiceSupport.getOrCreateAll(request.tags());
        Set<Long> requestedTagIds = requestedTags.stream()
                .map(Tag::getId)
                .collect(Collectors.toSet());

        List<StoreTag> currentTags = storeTagRepository.findAllByStoreIdOrderByTagNameAsc(storeId);

        currentTags.stream()
                .filter(storeTag -> storeTag.getSource() == StoreTagSource.MANUAL)
                .filter(storeTag -> !requestedTagIds.contains(storeTag.getTag().getId()))
                .forEach(storeTag -> removeManualStoreTag(storeId, storeTag));

        requestedTags.forEach(tag -> storeTagRepository.findByStoreIdAndTagId(storeId, tag.getId())
                .ifPresentOrElse(
                        StoreTag::markManual,
                        () -> storeTagRepository.save(StoreTag.create(store, tag, StoreTagSource.MANUAL))
                ));

        return getStoreTags(storeId);
    }

    private void removeManualStoreTag(Long storeId, StoreTag storeTag) {
        if (storeProductTagRepository.existsByStoreIdAndTagId(storeId, storeTag.getTag().getId())) {
            storeTag.markAuto();
            return;
        }
        storeTagRepository.delete(storeTag);
    }

    private void validateStoreExists(Long storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new ApiException(ErrorStatus.NOT_FOUND, "매장을 찾을 수 없습니다.");
        }
    }
}
