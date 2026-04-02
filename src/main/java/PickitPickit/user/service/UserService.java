package PickitPickit.user.service;

import PickitPickit.global.response.ErrorStatus;
import PickitPickit.user.converter.UserConverter;
import PickitPickit.user.dto.UserRequestDto;
import PickitPickit.user.dto.UserResponseDto;
import PickitPickit.user.entity.User;
import PickitPickit.user.entity.UserSetting;
import PickitPickit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    /* =========================
       유저 조회
       ========================= */

    @Transactional(readOnly = true)
    public UserResponseDto.UserInfo getUserInfo(Long userId) {

        User user = findUser(userId);

        return UserConverter.toUserInfo(user);
    }

    /* =========================
       프로필 수정
       ========================= */

    public void updateProfile(
            Long userId,
            UserRequestDto.UpdateProfile request
    ) {

        User user = findUser(userId);

        user.updateProfile(
                request.getNickname(),
                request.getProfileImageUrl()
        );
    }

    /* =========================
       설정 변경
       ========================= */

    public void updateSetting(
            Long userId,
            UserRequestDto.UpdateSetting request
    ) {

        User user = findUserWithSetting(userId);

        user.getSetting().updateSetting(
                request.getDistanceUnit(),
                request.getPushAlarmEnabled()
        );
    }

    /* =========================
       내부 조회 메서드
       ========================= */

    @Transactional(readOnly = true)
    protected User findUser(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(ErrorStatus.USER_NOT_FOUND.getMessage())
                );
    }

    /**
     * Setting 포함 조회
     * LazyInitialization + NPE 방지
     */
    @Transactional(readOnly = true)
    protected User findUserWithSetting(Long userId) {

        User user = findUser(userId);

        // Lazy 로딩 강제 초기화 (실무 패턴)
        UserSetting setting = user.getSetting();

        return user;
    }
}