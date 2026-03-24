alter table app_users
    add column linked_agent_id bigint;

alter table app_users
    add constraint fk_app_users_linked_agent
        foreign key (linked_agent_id) references agents (id);

-- Mantem o usuario padrao da ronda utilizavel em ambiente local ao vincular o primeiro vigilante disponivel.
update app_users
set linked_agent_id = (
    select a.id
    from agents a
    where a.status in ('ON_DUTY', 'ACTIVE')
    order by case when a.status = 'ON_DUTY' then 0 else 1 end, a.id
    fetch first 1 row only
)
where username = 'ronda'
  and role = 'RONDA'
  and linked_agent_id is null
  and exists (
      select 1
      from agents a
      where a.status in ('ON_DUTY', 'ACTIVE')
  );
