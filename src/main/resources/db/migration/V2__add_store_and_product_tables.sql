-- =============================================
-- V2: 매장(stores), 상품(items), 매장재고(store_products) 테이블 생성
-- =============================================

-- 매장 정보 (공공데이터 / 카카오맵 / 수기 등록 출처)
create table stores (
    id              bigserial       primary key,
    source_type     varchar(20)     not null,           -- PUBLIC_API / KAKAO_MAP / NAVER_MAP / MANUAL
    source_place_id varchar(100),                       -- 공공데이터 MNG_NO 또는 외부 API place_id (수기=null)

    name            varchar(255)    not null,
    store_type      varchar(20)     not null,           -- CLAW / GACHA
    address         varchar(500)    not null,
    latitude        decimal(10, 8)  not null,           -- WGS84 위도 (공공데이터 EPSG:5174 → 변환 후 저장)
    longitude       decimal(11, 8)  not null,           -- WGS84 경도
    contact         varchar(50),
    business_hours  varchar(255),
    main_image_url  varchar(500),

    created_at      timestamp       not null default current_timestamp,
    modified_at     timestamp       not null default current_timestamp,

    constraint uq_stores_source unique (source_type, source_place_id),

    constraint chk_stores_source_type
        check (source_type in ('PUBLIC_API', 'KAKAO_MAP', 'NAVER_MAP', 'MANUAL')),

    constraint chk_stores_store_type
        check (store_type in ('CLAW', 'GACHA'))
);

create index idx_stores_store_type on stores (store_type);
create index idx_stores_location on stores (latitude, longitude);


-- 상품 글로벌 카탈로그 (캐릭터/인형 마스터 정보)
create table items (
    id                bigserial       primary key,
    name              varchar(255)    not null,           -- 상품명 / 캐릭터명
    category          varchar(20)     not null,           -- PLUSHIE / FIGURE / GACHA
    default_image_url varchar(500),                       -- 기본 대표 사진 (AWS S3)

    created_at        timestamp       not null default current_timestamp,
    modified_at       timestamp       not null default current_timestamp,

    constraint chk_items_category
        check (category in ('PLUSHIE', 'FIGURE', 'GACHA'))
);

create index idx_items_name on items (name);
create index idx_items_category on items (category);


-- 매장별 재고 (매장 ↔ 상품 연결)
create table store_products (
    id              bigserial       primary key,
    store_id        bigint          not null references stores(id) on delete cascade,
    item_id         bigint          not null references items(id) on delete cascade,

    price           int,                                 -- 매장별 가격 (null = 가격 미확인)
    inventory_mode  varchar(20)     not null,            -- QUANTITY / STATUS
    stock_quantity  int,                                  -- 수량 기반 재고
    stock_status    varchar(20),                          -- IN_STOCK / OUT_OF_STOCK

    image_url       varchar(500),                        -- 매장별 상품 사진 (없으면 items.default_image_url)

    created_at      timestamp       not null default current_timestamp,
    modified_at     timestamp       not null default current_timestamp,

    constraint uq_store_products unique (store_id, item_id),

    constraint chk_store_products_inventory_mode
        check (inventory_mode in ('QUANTITY', 'STATUS')),

    constraint chk_store_products_stock_status
        check (stock_status is null or stock_status in ('IN_STOCK', 'OUT_OF_STOCK'))
);

create index idx_store_products_store on store_products (store_id);
create index idx_store_products_item on store_products (item_id);
create index idx_store_products_status on store_products (stock_status);
