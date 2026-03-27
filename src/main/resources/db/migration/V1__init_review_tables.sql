create table reviews (
                         id bigserial primary key,
                         user_id bigint not null,
                         store_id bigint not null,
                         rating double precision not null,
                         difficulty integer null,
                         content text null,
                         image_url varchar(255) null,
                         created_at timestamp not null default current_timestamp,
                         modified_at timestamp not null default current_timestamp,
                         constraint uk_review_user_store unique (user_id, store_id),
                         constraint chk_reviews_rating_range
                             check (rating >= 0.5 and rating <= 5.0),
                         constraint chk_reviews_rating_step
                             check (((rating * 10)::int % 5) = 0),
    constraint chk_reviews_difficulty
        check (difficulty is null or (difficulty between 1 and 5))
);

create table brags (
                       id bigserial primary key,
                       user_id bigint not null,
                       store_id bigint null,
                       spent_cost integer not null,
                       image_url varchar(255) not null,
                       content text null,
                       created_at timestamp not null default current_timestamp,
                       modified_at timestamp not null default current_timestamp,
                       constraint chk_brags_spent_cost
                           check (spent_cost >= 0)
);