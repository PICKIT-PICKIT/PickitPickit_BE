package PickitPickit.user.converter;

import PickitPickit.user.dto.UserResponseDto;
import PickitPickit.user.entity.User;

public class UserConverter {

    public static UserResponseDto.UserInfo toUserInfo(User user) {

        return UserResponseDto.UserInfo.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .distanceUnit(user.getSetting().getDistanceUnit())
                .pushAlarmEnabled(
                        user.getSetting().isPushAlarmEnabled()
                )
                .build();
    }
}