package PickitPickit.global.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SuccessStatus {

    // Auth
    SIGNUP_SUCCESS(HttpStatus.CREATED, "SIGNUP_SUCCESS", "회원가입이 완료되었습니다."),
    LOGIN_SUCCESS(HttpStatus.OK, "LOGIN_SUCCESS", "로그인에 성공했습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "LOGOUT_SUCCESS", "로그아웃 되었습니다."),

    // Common CRUD
    CREATED(HttpStatus.CREATED, "CREATED", "리소스가 생성되었습니다."),
    OK(HttpStatus.OK, "OK", "요청이 성공적으로 처리되었습니다."),
    UPDATED(HttpStatus.OK, "UPDATED", "리소스가 수정되었습니다."),
    DELETED(HttpStatus.OK, "DELETED", "리소스가 삭제되었습니다."),
    FETCHED(HttpStatus.OK, "FETCHED", "데이터 조회 성공"),

    /* =========================
       USER (추가 부분 ✅)
       ========================= */

    USER_INFO_SUCCESS(
            HttpStatus.OK,
            "USER_INFO_SUCCESS",
            "유저 정보 조회 성공"
    ),

    USER_UPDATE_SUCCESS(
            HttpStatus.OK,
            "USER_UPDATE_SUCCESS",
            "프로필 수정 성공"
    ),

    USER_SETTING_UPDATE_SUCCESS(
            HttpStatus.OK,
            "USER_SETTING_UPDATE_SUCCESS",
            "유저 설정 변경 성공"
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

    SuccessStatus(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}