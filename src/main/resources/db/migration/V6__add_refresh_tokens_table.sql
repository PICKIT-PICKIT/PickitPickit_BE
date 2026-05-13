create table refresh_tokens (
    id bigserial primary key,
    user_id bigint not null references users(id) on delete cascade,
    token_hash varchar(64) not null,
    expires_at timestamp not null,
    created_at timestamp not null default current_timestamp,
    modified_at timestamp not null default current_timestamp,

    constraint uq_refresh_tokens_token_hash unique (token_hash)
);

create index idx_refresh_tokens_user_id on refresh_tokens (user_id);
create index idx_refresh_tokens_expires_at on refresh_tokens (expires_at);
