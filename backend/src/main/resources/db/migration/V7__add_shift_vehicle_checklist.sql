alter table shifts
    add column fuel_level_percent integer,
    add column tires_checked boolean not null default false,
    add column lights_checked boolean not null default false,
    add column documents_checked boolean not null default false,
    add column checklist_notes varchar(500);
