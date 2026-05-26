package PickitPickit.store.service;

import PickitPickit.store.domain.Store;
import PickitPickit.store.domain.StoreTag;
import PickitPickit.store.domain.StoreTagSource;
import PickitPickit.store.domain.Tag;
import PickitPickit.store.repository.StoreProductTagRepository;
import PickitPickit.store.repository.StoreTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreTagSyncService {

    private final StoreTagRepository storeTagRepository;
    private final StoreProductTagRepository storeProductTagRepository;

    @Transactional
    public void syncAutoTagsAfterProductTagChange(Store store, List<Tag> productTags) {
        if (productTags != null) {
            productTags.forEach(tag -> ensureAutoStoreTag(store, tag));
        }
        cleanupUnusedAutoTags(store.getId());
    }

    @Transactional
    public void cleanupUnusedAutoTags(Long storeId) {
        List<StoreTag> autoTags = storeTagRepository.findAllByStoreIdAndSourceOrderByTagNameAsc(
                storeId,
                StoreTagSource.AUTO
        );

        autoTags.stream()
                .filter(storeTag -> !storeProductTagRepository.existsByStoreIdAndTagId(
                        storeId,
                        storeTag.getTag().getId()
                ))
                .forEach(storeTagRepository::delete);
    }

    private void ensureAutoStoreTag(Store store, Tag tag) {
        storeTagRepository.findByStoreIdAndTagId(store.getId(), tag.getId())
                .orElseGet(() -> storeTagRepository.save(StoreTag.create(store, tag, StoreTagSource.AUTO)));
    }
}
