package PickitPickit.store.loader;

import PickitPickit.store.client.PublicApiResponse;
import PickitPickit.store.domain.MapSourceType;
import PickitPickit.store.domain.Store;
import PickitPickit.store.dto.StoreType;
import PickitPickit.store.repository.StoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.proj4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class PublicApiStoreLoader {

    private static final String ACTIVE_STATUS_CODE = "01";
    private static final Set<String> GACHA_KEYWORDS = Set.of("가챠", "가차", "캡슐토이", "가샤폰");

    private final StoreRepository storeRepository;
    private final String serviceKey;
    private final String baseUrl;

    private static final String EPSG_5174_PARAMS = "+proj=tmerc +lat_0=38 +lon_0=127.0028902777778 +k=1 " +
            "+x_0=200000 +y_0=500000 +ellps=bessel +units=m +no_defs " +
            "+towgs84=-115.80,474.99,674.11,1.16,-2.31,-1.63,6.43";

    private static final String WGS84_PARAMS = "+proj=longlat +datum=WGS84 +no_defs";

    private final CoordinateTransform coordTransform;

    public PublicApiStoreLoader(StoreRepository storeRepository,
                                @Value("${public-api.service-key}") String serviceKey,
                                @Value("${public-api.base-url}") String baseUrl) {
        this.storeRepository = storeRepository;
        this.serviceKey = serviceKey;
        this.baseUrl = baseUrl;

        CRSFactory crsFactory = new CRSFactory();
        CoordinateReferenceSystem source = crsFactory.createFromParameters("EPSG:5174", EPSG_5174_PARAMS);
        CoordinateReferenceSystem target = crsFactory.createFromParameters("EPSG:4326", WGS84_PARAMS);
        this.coordTransform = new CoordinateTransformFactory().createTransform(source, target);
    }

    public int loadAll() {
        int page = 1;
        int numOfRows = 100;
        int totalLoaded = 0;
        int totalSkipped = 0;
        Set<String> seenSourcePlaceIds = new HashSet<>();

        while (true) {
            log.info("[공공데이터 로더] 페이지 {} 조회 중...", page);
            PublicApiResponse response = fetchPage(page, numOfRows);

            if (response == null || response.getResponse() == null
                    || response.getResponse().getBody() == null
                    || response.getResponse().getBody().getItems() == null
                    || response.getResponse().getBody().getItems().getItem() == null) {
                log.info("[공공데이터 로더] 더 이상 데이터가 없습니다. (page={})", page);
                break;
            }

            List<PublicApiResponse.Item> items = response.getResponse().getBody().getItems().getItem();
            if (items.isEmpty()) {
                break;
            }

            List<Store> toSave = new ArrayList<>();
            for (PublicApiResponse.Item item : items) {
                String sourcePlaceId = buildPublicSourcePlaceId(item);
                if (sourcePlaceId == null || !seenSourcePlaceIds.add(sourcePlaceId)) {
                    totalSkipped++;
                    continue;
                }

                Store store = processItem(item, sourcePlaceId);
                if (store != null) {
                    toSave.add(store);
                } else {
                    totalSkipped++;
                }
            }

            if (!toSave.isEmpty()) {
                storeRepository.saveAll(toSave);
                totalLoaded += toSave.size();
            }

            int totalCount = response.getResponse().getBody().getTotalCount();
            if (page * numOfRows >= totalCount) {
                break;
            }
            page++;
        }

        log.info("[공공데이터 로더] 완료. 적재: {}건, 스킵: {}건", totalLoaded, totalSkipped);
        return totalLoaded;
    }

    private Store processItem(PublicApiResponse.Item item, String sourcePlaceId) {
        if (!ACTIVE_STATUS_CODE.equals(item.getSalesStatusCode())) {
            return null;
        }

        if (item.getCoordX() == null || item.getCoordX().isBlank()
                || item.getCoordY() == null || item.getCoordY().isBlank()) {
            log.debug("[스킵] 좌표 없음: {} (sourcePlaceId={})", item.getBusinessName(), sourcePlaceId);
            return null;
        }

        if (storeRepository.existsBySourceTypeAndSourcePlaceId(MapSourceType.PUBLIC_API, sourcePlaceId)) {
            return null;
        }

        double[] wgs84 = transformCoordinate(
                Double.parseDouble(item.getCoordX()),
                Double.parseDouble(item.getCoordY())
        );
        if (wgs84 == null) {
            log.warn("[스킵] 좌표 변환 실패: {} (X={}, Y={})", item.getBusinessName(), item.getCoordX(), item.getCoordY());
            return null;
        }

        String address = (item.getRoadAddress() != null && !item.getRoadAddress().isBlank())
                ? item.getRoadAddress()
                : item.getLotAddress();
        if (address == null || address.isBlank()) {
            return null;
        }

        if (!address.startsWith("서울") && !address.startsWith("경기")) {
            log.debug("[스킵] 서울/경기가 아님: {} (주소={})", item.getBusinessName(), address);
            return null;
        }

        StoreType storeType = classifyStoreType(item.getBusinessName());

        return Store.createFromPublicApi(
                sourcePlaceId,
                item.getBusinessName(),
                storeType,
                address,
                BigDecimal.valueOf(wgs84[0]).setScale(8, RoundingMode.HALF_UP),
                BigDecimal.valueOf(wgs84[1]).setScale(8, RoundingMode.HALF_UP),
                item.getPhone()
        );
    }

    private String buildPublicSourcePlaceId(PublicApiResponse.Item item) {
        if (item == null || item.getRegionCode() == null || item.getRegionCode().isBlank()
                || item.getMngNo() == null || item.getMngNo().isBlank()) {
            return null;
        }
        return item.getRegionCode().trim() + ":" + item.getMngNo().trim();
    }

    private StoreType classifyStoreType(String businessName) {
        if (businessName == null) {
            return StoreType.CLAW;
        }
        for (String keyword : GACHA_KEYWORDS) {
            if (businessName.contains(keyword)) {
                return StoreType.GACHA;
            }
        }
        return StoreType.CLAW;
    }

    private double[] transformCoordinate(double x, double y) {
        try {
            ProjCoordinate src = new ProjCoordinate(x, y);
            ProjCoordinate dst = new ProjCoordinate();
            coordTransform.transform(src, dst);
            return new double[]{dst.y, dst.x};
        } catch (Exception e) {
            log.warn("좌표 변환 실패: x={}, y={}, error={}", x, y, e.getMessage());
            return null;
        }
    }

    private PublicApiResponse fetchPage(int page, int numOfRows) {
        URI uri = UriComponentsBuilder
                .fromUriString(baseUrl)
                .queryParam("serviceKey", serviceKey)
                .queryParam("pageNo", page)
                .queryParam("numOfRows", numOfRows)
                .queryParam("returnType", "JSON")
                .queryParam("cond[SALS_STTS_CD::EQ]", ACTIVE_STATUS_CODE)
                .build()
                .encode()
                .toUri();

        try {
            return RestClient.create()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .body(PublicApiResponse.class);
        } catch (RestClientException e) {
            log.error("[공공데이터 로더] API 호출 실패 (page={}): {}", page, e.getMessage());
            return null;
        }
    }
}
