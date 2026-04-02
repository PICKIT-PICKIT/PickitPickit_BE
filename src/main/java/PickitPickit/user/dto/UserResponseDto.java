package PickitPickit.user.dto;

import PickitPickit.user.entity.DistanceUnit;
import lombok.Builder;
import lombok.Getter;

public class UserResponseDto {

    @Getter
    @Builder
    public static class UserInfo {

        private Long userId;
        private String nickname;
        private String email;
        private String profileImageUrl;

        private DistanceUnit distanceUnit;
        private boolean pushAlarmEnabled;
    }
}