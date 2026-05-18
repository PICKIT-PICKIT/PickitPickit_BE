delete from refresh_tokens old_token
using refresh_tokens latest_token
where old_token.user_id = latest_token.user_id
  and old_token.id < latest_token.id;

alter table refresh_tokens
    add constraint uq_refresh_tokens_user_id unique (user_id);
