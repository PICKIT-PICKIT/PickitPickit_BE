package PickitPickit.user.dto;

import PickitPickit.user.entity.DistanceUnit;
import lombok.Getter;

public class UserRequestDto {

    @Getter
    public static class UpdateProfile {

        private String nickname;
        private String profileImageUrl;
    }

    @Getter
    public static class UpdateSetting {

        private DistanceUnit distanceUnit;
        private Boolean pushAlarmEnabled;
    }
}