package PickitPickit.store.service;

import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import PickitPickit.store.domain.FavoriteStore;
import PickitPickit.store.domain.Store;
import PickitPickit.store.dto.FavoriteStoreResponse;
import PickitPickit.store.repository.FavoriteStoreRepository;
import PickitPickit.store.repository.StoreRepository;
import PickitPickit.user.domain.User;
import PickitPickit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteStoreServiceImpl implements FavoriteStoreService {

    private final FavoriteStoreRepository favoriteStoreRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public FavoriteStoreResponse addFavoriteStore(Long userId, Long storeId) {
        User user = getUser(userId);
        Store store = getStore(storeId);

        if (favoriteStoreRepository.existsByUserIdAndStoreId(userId, storeId)) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "이미 관심매장으로 등록된 매장입니다.");
        }

        FavoriteStore favoriteStore = favoriteStoreRepository.save(
                FavoriteStore.create(user, store)
        );

        return FavoriteStoreResponse.from(favoriteStore, 0);
    }

    @Override
    @Transactional
    public void removeFavoriteStore(Long userId, Long storeId) {
        FavoriteStore favoriteStore = favoriteStoreRepository.findByUserIdAndStoreId(userId, storeId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND, "관심매장으로 등록된 매장이 아닙니다."));

        favoriteStoreRepository.delete(favoriteStore);
    }

    @Override
    public List<FavoriteStoreResponse> getMyFavoriteStores(Long userId, Double userLat, Double userLng) {
        getUser(userId);

        if ((userLat == null) != (userLng == null)) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "lat, lng는 둘 다 입력하거나 둘 다 생략해야 합니다.");
        }

        boolean hasLocation = userLat != null && userLng != null;

        if (hasLocation) {
            validateCoordinate(userLat, userLng);
        }

        return favoriteStoreRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(favoriteStore -> FavoriteStoreResponse.from(
                        favoriteStore,
                        hasLocation
                                ? calculateDistance(
                                        userLat,
                                        userLng,
                                        favoriteStore.getStore().getLatitude().doubleValue(),
                                        favoriteStore.getStore().getLongitude().doubleValue()
                                )
                                : 0
                ))
                .toList();
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorStatus.USER_NOT_FOUND, "해당 사용자를 찾을 수 없습니다."));
    }

    private Store getStore(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND, "매장을 찾을 수 없습니다."));
    }

    private void validateCoordinate(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "위도(lat)는 -90 이상 90 이하여야 합니다.");
        }

        if (longitude < -180 || longitude > 180) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "경도(lng)는 -180 이상 180 이하여야 합니다.");
        }
    }

    private int calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6_371_000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (int) Math.round(earthRadius * c);
    }
}