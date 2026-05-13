package PickitPickit.auth.client;

import PickitPickit.auth.dto.client.KakaoUserResponse;
import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class KakaoAuthClient {

    private final RestClient restClient;
    private final String userInfoUrl;

    public KakaoAuthClient(
            @Value("${kakao.oauth.user-info-url:https://kapi.kakao.com/v2/user/me}") String userInfoUrl
    ) {
        this.restClient = RestClient.create();
        this.userInfoUrl = userInfoUrl;
    }

    public KakaoUserResponse getUserInfo(String kakaoAccessToken) {
        try {
            return restClient.get()
                    .uri(userInfoUrl)
                    .header("Authorization", "Bearer " + kakaoAccessToken)
                    .retrieve()
                    .body(KakaoUserResponse.class);
        } catch (HttpStatusCodeException e) {
            log.warn("카카오 사용자 정보 조회 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ApiException(ErrorStatus.KAKAO_LOGIN_FAILED, "유효하지 않은 카카오 액세스 토큰입니다.");
        } catch (RestClientException e) {
            log.warn("카카오 사용자 정보 조회 실패: {}", e.getMessage());
            throw new ApiException(ErrorStatus.KAKAO_LOGIN_FAILED, "유효하지 않은 카카오 액세스 토큰입니다.");
        }
    }
}
