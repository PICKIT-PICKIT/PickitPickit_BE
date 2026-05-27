package PickitPickit.review.service;

import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import PickitPickit.review.domain.Brag;
import PickitPickit.review.dto.request.BragCreateRequest;
import PickitPickit.review.dto.request.BragUpdateRequest;
import PickitPickit.review.dto.response.BragResponse;
import PickitPickit.review.repository.BragRepository;
import PickitPickit.user.domain.User;
import PickitPickit.user.domain.UserStatus;
import PickitPickit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BragServiceImpl implements BragService {

    private final BragRepository bragRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BragResponse createBrag(BragCreateRequest request) {
        User user = getActiveUser(request.userId());

        Brag brag = Brag.create(
                request.userId(),
                request.storeId(),
                request.spentCost(),
                request.imageUrl(),
                request.content()
        );

        return BragResponse.from(bragRepository.save(brag), user);
    }

    @Override
    public List<BragResponse> getBrags() {
        List<Brag> brags = bragRepository.findAllByOrderByCreatedAtDesc();

        Map<Long, User> authorMap = getAuthorMap(brags.stream()
                .map(Brag::getUserId)
                .toList());

        return brags.stream()
                .map(brag -> BragResponse.from(brag, authorMap.get(brag.getUserId())))
                .toList();
    }

    @Override
    public List<BragResponse> getMyBrags(Long userId) {
        User user = getActiveUser(userId);

        return bragRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(brag -> BragResponse.from(brag, user))
                .toList();
    }

    @Override
    @Transactional
    public BragResponse updateBrag(Long bragId, BragUpdateRequest request) {
        User user = getActiveUser(request.userId());

        Brag brag = bragRepository.findByIdAndUserId(bragId, request.userId())
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND, "수정할 자랑글을 찾을 수 없습니다."));

        brag.update(
                request.storeId(),
                request.spentCost(),
                request.imageUrl(),
                request.content()
        );

        return BragResponse.from(brag, user);
    }

    @Override
    @Transactional
    public void deleteBrag(Long bragId, Long userId) {
        getActiveUser(userId);

        Brag brag = bragRepository.findByIdAndUserId(bragId, userId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND, "삭제할 자랑글을 찾을 수 없습니다."));

        bragRepository.delete(brag);
    }

    private User getActiveUser(Long userId) {
        return userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(ErrorStatus.USER_NOT_FOUND, "해당 사용자를 찾을 수 없습니다."));
    }

    private Map<Long, User> getAuthorMap(List<Long> userIds) {
        return userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }
}