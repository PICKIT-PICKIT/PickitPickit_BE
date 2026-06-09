-- 1. 사용자 권한
alter table users
    add column if not exists role varchar(20) not null default 'USER';

alter table users
    drop constraint if exists chk_users_role;

alter table users
    add constraint chk_users_role
        check (role in ('USER', 'STORE_OWNER', 'ADMIN'));

-- 2. stores.source_place_id 보강
-- 최종 코드에서는 source_place_id가 nullable=false이며,
-- PUBLIC_API는 regionCode:mngNo 형태를 사용합니다.
alter table stores
    alter column source_place_id type varchar(150);

update stores
set source_place_id = source_type || ':' || id
where source_place_id is null or trim(source_place_id) = '';

alter table stores
    alter column source_place_id set not null;

-- 3. items / store_products enum check 제약 보정
-- 기존 V2는 PLUSHIE, IN_STOCK, OUT_OF_STOCK만 허용하므로 최종 enum과 충돌합니다.
alter table items
    drop constraint if exists chk_items_category;

update items
set category = 'PLUSH'
where category = 'PLUSHIE';

alter table items
    add constraint chk_items_category
        check (category in ('PLUSH', 'FIGURE', 'KEYRING', 'GACHA', 'SNACK', 'ETC'));

alter table store_products
    drop constraint if exists chk_store_products_stock_status;

alter table store_products
    add column if not exists difficulty integer;

update store_products
set stock_status = 'UNKNOWN'
where stock_status is null;

alter table store_products
    alter column stock_status set not null;

alter table store_products
    drop constraint if exists chk_store_products_difficulty;

alter table store_products
    add constraint chk_store_products_difficulty
        check (difficulty is null or difficulty between 1 and 5);

alter table store_products
    add constraint chk_store_products_stock_status
        check (stock_status in ('IN_STOCK', 'LOW_STOCK', 'OUT_OF_STOCK', 'UNKNOWN'));

-- 4. 공통 태그
create table if not exists tags (
    id bigserial primary key,
    name varchar(50) not null,
    normalized_name varchar(50) not null,
    created_at timestamp not null default current_timestamp,
    modified_at timestamp not null default current_timestamp,

    constraint uq_tags_normalized_name unique (normalized_name)
);

create index if not exists idx_tags_name on tags(name);

-- 5. 상품 마스터 태그
create table if not exists item_tags (
    id bigserial primary key,
    item_id bigint not null,
    tag_id bigint not null,
    created_at timestamp not null default current_timestamp,
    modified_at timestamp not null default current_timestamp,

    constraint uq_item_tags unique (item_id, tag_id),
    constraint fk_item_tags_item foreign key (item_id) references items(id) on delete cascade,
    constraint fk_item_tags_tag foreign key (tag_id) references tags(id) on delete cascade
);

create index if not exists idx_item_tags_item_id on item_tags(item_id);
create index if not exists idx_item_tags_tag_id on item_tags(tag_id);

-- 6. 매장 대표 태그
create table if not exists store_tags (
    id bigserial primary key,
    store_id bigint not null,
    tag_id bigint not null,
    source varchar(20) not null default 'AUTO',
    created_at timestamp not null default current_timestamp,
    modified_at timestamp not null default current_timestamp,

    constraint uq_store_tags unique (store_id, tag_id),
    constraint fk_store_tags_store foreign key (store_id) references stores(id) on delete cascade,
    constraint fk_store_tags_tag foreign key (tag_id) references tags(id) on delete cascade
);

alter table store_tags
    drop constraint if exists chk_store_tags_source;

alter table store_tags
    add constraint chk_store_tags_source
        check (source in ('MANUAL', 'AUTO'));

create index if not exists idx_store_tags_store_id on store_tags(store_id);
create index if not exists idx_store_tags_tag_id on store_tags(tag_id);
create index if not exists idx_store_tags_source on store_tags(source);

-- 7. 매장별 상품 태그
create table if not exists store_product_tags (
    id bigserial primary key,
    store_product_id bigint not null,
    tag_id bigint not null,
    created_at timestamp not null default current_timestamp,
    modified_at timestamp not null default current_timestamp,

    constraint uq_store_product_tags unique (store_product_id, tag_id),
    constraint fk_store_product_tags_product foreign key (store_product_id) references store_products(id) on delete cascade,
    constraint fk_store_product_tags_tag foreign key (tag_id) references tags(id) on delete cascade
);

create index if not exists idx_store_product_tags_product_id on store_product_tags(store_product_id);
create index if not exists idx_store_product_tags_tag_id on store_product_tags(tag_id);

-- 8. 매장주 / 직원 연결
create table if not exists store_managers (
    id bigserial primary key,
    store_id bigint not null,
    user_id bigint not null,
    role varchar(20) not null default 'OWNER',
    created_at timestamp not null default current_timestamp,
    modified_at timestamp not null default current_timestamp,

    constraint uq_store_manager unique (store_id, user_id),
    constraint fk_store_managers_store foreign key (store_id) references stores(id) on delete cascade,
    constraint fk_store_managers_user foreign key (user_id) references users(id) on delete cascade
);

alter table store_managers
    drop constraint if exists chk_store_managers_role;

alter table store_managers
    add constraint chk_store_managers_role
        check (role in ('OWNER', 'STAFF'));

create index if not exists idx_store_managers_store_id on store_managers(store_id);
create index if not exists idx_store_managers_user_id on store_managers(user_id);

-- 최초 관리자 지정은 운영자가 직접 실행:
-- update users set role = 'ADMIN' where kakao_id = '카카오_회원번호';
