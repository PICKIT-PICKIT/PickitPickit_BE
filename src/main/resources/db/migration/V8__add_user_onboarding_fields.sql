alter table users
    add column kakao_profile_image_url varchar(500),
    add column profile_image_type varchar(20),
    add column onboarding_completed boolean not null default false;

update users
set kakao_profile_image_url = profile_image_url
where kakao_profile_image_url is null
  and profile_image_url is not null;

update users
set profile_image_type = 'KAKAO'
where profile_image_url is not null
  and profile_image_type is null;

with duplicated_users as (
    select id,
           row_number() over (partition by nickname order by id) as rn
    from users
)
update users u
set nickname = 'pickit_' || u.id
from duplicated_users d
where u.id = d.id
  and d.rn > 1;

alter table users
    add constraint uq_users_nickname unique (nickname);

