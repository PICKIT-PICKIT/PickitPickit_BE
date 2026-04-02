create table users (
    id bigserial primary key,
    kakao_id varchar(100) not null,
    nickname varchar(100) not null,
    profile_image_url varchar(500) not null,
    created_at timestamp not null default current_timestamp,
    modified_at timestamp not null default current_timestamp,

    constraint uq_users_kakao_id unique (kakao_id)
);

create index idx_users_created_at on users (created_at);
