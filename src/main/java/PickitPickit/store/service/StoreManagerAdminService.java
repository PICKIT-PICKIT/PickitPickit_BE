package PickitPickit.store.service;

import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import PickitPickit.store.domain.Store;
import PickitPickit.store.domain.StoreManager;
import PickitPickit.store.dto.StoreManagerCreateRequest;
import PickitPickit.store.dto.StoreManagerResponse;
import PickitPickit.store.repository.StoreManagerRepository;
import PickitPickit.store.repository.StoreRepository;
import PickitPickit.user.domain.User;
import PickitPickit.user.domain.UserRole;
import PickitPickit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreManagerAdminService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StoreManagerRepository storeManagerRepository;

    @Transactional
    public StoreManagerResponse createStoreManager(StoreManagerCreateRequest request) {
        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND, "매장을 찾을 수 없습니다."));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ApiException(ErrorStatus.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (storeManagerRepository.existsByStoreIdAndUserId(store.getId(), user.getId())) {
            throw new ApiException(ErrorStatus.DUPLICATE_RESOURCE, "이미 등록된 매장 관리자입니다.");
        }

        if (user.getRole() == UserRole.USER) {
            user.changeRole(UserRole.STORE_OWNER);
        }

        StoreManager storeManager = StoreManager.create(store, user, request.role());
        return StoreManagerResponse.from(storeManagerRepository.save(storeManager));
    }
}
