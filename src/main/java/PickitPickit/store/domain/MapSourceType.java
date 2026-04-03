package PickitPickit.store.domain;

/**
 * 매장 데이터 출처
 */
public enum MapSourceType {
    PUBLIC_API,   // 공공데이터 포털 (청소년게임제공업)
    KAKAO_MAP,    // 카카오 로컬 키워드 검색
    NAVER_MAP,    // 네이버 지도
    MANUAL        // 수기 등록
}
