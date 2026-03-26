package PickitPickit.review.service;

import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import PickitPickit.review.domain.Brag;
import PickitPickit.review.dto.request.BragCreateRequest;
import PickitPickit.review.dto.request.BragUpdateRequest;
import PickitPickit.review.dto.response.BragResponse;
import PickitPickit.review.repository.BragRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BragServiceImpl implements BragService {

    private final BragRepository bragRepository;

    @Override
    @Transactional
    public BragResponse createBrag(BragCreateRequest request) {
        Brag brag = Brag.create(
                request.userId(),
                request.storeId(),
                request.spentCost(),
                request.imageUrl(),
                request.content()
        );

        return BragResponse.from(bragRepository.save(brag));
    }

    @Override
    public List<BragResponse> getBrags() {
        return bragRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(BragResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public BragResponse updateBrag(Long bragId, BragUpdateRequest request) {
        Brag brag = bragRepository.findByIdAndUserId(bragId, request.userId())
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND, "수정할 자랑글을 찾을 수 없습니다."));

        brag.update(
                request.storeId(),
                request.spentCost(),
                request.imageUrl(),
                request.content()
        );

        return BragResponse.from(brag);
    }

    @Override
    @Transactional
    public void deleteBrag(Long bragId, Long userId) {
        Brag brag = bragRepository.findByIdAndUserId(bragId, userId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND, "삭제할 자랑글을 찾을 수 없습니다."));

        bragRepository.delete(brag);
    }
}