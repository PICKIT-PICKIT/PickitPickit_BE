package PickitPickit.store.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 공공데이터 포털 - 청소년게임제공업 API 응답 DTO
 * https://www.data.go.kr/data/15154958/openapi.do
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublicApiResponse {

    private Response response;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private Body body;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        private Items items;
        private int totalCount;
        private int numOfRows;
        private int pageNo;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {
        private List<Item> item;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {

        /** 관리번호 (고유 식별자) */
        @JsonProperty("MNG_NO")
        private String mngNo;

        /** 사업장명 */
        @JsonProperty("BPLC_NM")
        private String businessName;

        /** 영업상태코드 (01=영업/정상) */
        @JsonProperty("SALS_STTS_CD")
        private String salesStatusCode;

        /** 영업상태명 */
        @JsonProperty("SALS_STTS_NM")
        private String salesStatusName;

        /** X좌표 (EPSG:5174 중부원점TM) */
        @JsonProperty("CRD_INFO_X")
        private String coordX;

        /** Y좌표 (EPSG:5174 중부원점TM) */
        @JsonProperty("CRD_INFO_Y")
        private String coordY;

        /** 도로명주소 */
        @JsonProperty("ROAD_NM_ADDR")
        private String roadAddress;

        /** 지번주소 */
        @JsonProperty("LOTNO_ADDR")
        private String lotAddress;

        /** 전화번호 */
        @JsonProperty("TELNO")
        private String phone;

        /** 총게임기수 */
        @JsonProperty("TOTAL_GMCON_CNT")
        private String totalGameCount;

        /** 인허가일자 */
        @JsonProperty("LCPMT_YMD")
        private String licenseDate;

        /** 개방자치단체코드 */
        @JsonProperty("OPN_ATMY_GRP_CD")
        private String regionCode;
    }
}
