create table interest_tags (
    id bigserial primary key,
    name varchar(50) not null,
    display_order integer not null,
    active boolean not null default true,
    created_at timestamp not null default current_timestamp,
    modified_at timestamp not null default current_timestamp,

    constraint uq_interest_tags_name unique (name)
);

create table user_interest_tags (
    id bigserial primary key,
    user_id bigint not null,
    interest_tag_id bigint not null,
    created_at timestamp not null default current_timestamp,
    modified_at timestamp not null default current_timestamp,

    constraint fk_user_interest_tags_user foreign key (user_id) references users (id) on delete cascade,
    constraint fk_user_interest_tags_interest_tag foreign key (interest_tag_id) references interest_tags (id) on delete cascade,
    constraint uq_user_interest_tags_user_tag unique (user_id, interest_tag_id)
);

create index idx_interest_tags_active_order on interest_tags (active, display_order);
create index idx_user_interest_tags_user_id on user_interest_tags (user_id);

insert into interest_tags (name, display_order)
values
    ('포켓몬', 1),
    ('디즈니', 2),
    ('원피스', 3),
    ('산리오', 4),
    ('마블', 5),
    ('BT21', 6),
    ('짱구', 7),
    ('팬텀', 8),
    ('귀멸의칼날', 9),
    ('나루토', 10),
    ('카카오', 11),
    ('지브리', 12),
    ('메이플', 13),
    ('스누피', 14),
    ('드래곤볼', 15);
