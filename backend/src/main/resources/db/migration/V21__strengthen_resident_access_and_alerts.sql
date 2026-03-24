alter table residents
    add column access_pin_hash varchar(255),
    add column coercion_pin_hash varchar(255);

alter table resident_alerts
    add column silent boolean not null default false,
    add column escort_destination varchar(255);

-- Mantem o ambiente local utilizavel: o PIN de acesso padrao vira os 4 ultimos digitos do telefone,
-- e o PIN de coacao vira esses mesmos 4 digitos em ordem inversa.
update residents
set access_pin_hash = '{noop}' || right(regexp_replace(phone_number, '\D', '', 'g'), 4),
    coercion_pin_hash = '{noop}' || reverse(right(regexp_replace(phone_number, '\D', '', 'g'), 4))
where access_pin_hash is null
   or coercion_pin_hash is null;
