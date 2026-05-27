package PickitPickit.mypage.service;

import PickitPickit.mypage.dto.request.MyPageProfileUpdateRequest;
import PickitPickit.mypage.dto.response.MyPageProfileResponse;

public interface MyPageService {

    MyPageProfileResponse getProfile(Long userId);

    MyPageProfileResponse updateProfile(Long userId, MyPageProfileUpdateRequest request);
}