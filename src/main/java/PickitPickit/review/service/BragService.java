package PickitPickit.review.service;

import PickitPickit.review.dto.request.BragCreateRequest;
import PickitPickit.review.dto.request.BragUpdateRequest;
import PickitPickit.review.dto.response.BragResponse;

import java.util.List;

public interface BragService {

    BragResponse createBrag(BragCreateRequest request);

    List<BragResponse> getBrags();

    BragResponse updateBrag(Long bragId, BragUpdateRequest request);

    void deleteBrag(Long bragId, Long userId);
}