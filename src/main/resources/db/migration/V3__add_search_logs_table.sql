create table search_logs (
    id bigserial primary key,
    user_id bigint,
    keyword varchar(255) not null,
    target_type varchar(20) not null,
    searched_at timestamp not null default current_timestamp,

    constraint chk_search_logs_target_type
        check (target_type in ('STORE', 'ITEM', 'ALL'))
);

create index idx_search_logs_user_searched_at on search_logs (user_id, searched_at desc);
create index idx_search_logs_keyword on search_logs (keyword);
