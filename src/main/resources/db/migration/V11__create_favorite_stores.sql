-- 관심 매장 추가
create table favorite_stores (
                                 id bigserial primary key,
                                 user_id bigint not null,
                                 store_id bigint not null,
                                 created_at timestamp not null,
                                 updated_at timestamp not null,

                                 constraint uq_favorite_stores_user_store unique (user_id, store_id),
                                 constraint fk_favorite_stores_user foreign key (user_id) references users(id),
                                 constraint fk_favorite_stores_store foreign key (store_id) references stores(id)
);

create index idx_favorite_stores_user_id on favorite_stores(user_id);
create index idx_favorite_stores_store_id on favorite_stores(store_id);