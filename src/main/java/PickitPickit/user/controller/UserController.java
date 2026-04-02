package PickitPickit.user.controller;

import PickitPickit.global.response.ApiResponse;
import PickitPickit.global.response.SuccessStatus;
import PickitPickit.user.dto.UserRequestDto;
import PickitPickit.user.dto.UserResponseDto;
import PickitPickit.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 유저 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto.UserInfo>> getUserInfo() {

        Long userId = 1L; // TODO: 로그인 연동

        return ApiResponse.success(
                SuccessStatus.USER_INFO_SUCCESS,
                userService.getUserInfo(userId)
        );
    }

    // 프로필 수정
    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            @RequestBody UserRequestDto.UpdateProfile request
    ) {

        Long userId = 1L;

        userService.updateProfile(userId, request);

        return ApiResponse.success(
                SuccessStatus.USER_UPDATE_SUCCESS
        );
    }

    // 설정 변경
    @PatchMapping("/setting")
    public ResponseEntity<ApiResponse<Void>> updateSetting(
            @RequestBody UserRequestDto.UpdateSetting request
    ) {

        Long userId = 1L;

        userService.updateSetting(userId, request);

        return ApiResponse.success(
                SuccessStatus.USER_SETTING_UPDATE_SUCCESS
        );
    }
}