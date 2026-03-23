do $$
declare
    shift_unique_constraint text;
begin
    select tc.constraint_name
      into shift_unique_constraint
      from information_schema.table_constraints tc
      join information_schema.constraint_column_usage ccu
        on tc.constraint_name = ccu.constraint_name
       and tc.table_schema = ccu.table_schema
     where tc.table_schema = 'public'
       and tc.table_name = 'shift_telemetry'
       and tc.constraint_type = 'UNIQUE'
       and ccu.column_name = 'shift_id'
     limit 1;

    if shift_unique_constraint is not null then
        execute format('alter table shift_telemetry drop constraint %I', shift_unique_constraint);
    end if;
end $$;

create index if not exists idx_shift_telemetry_shift_recorded_at
    on shift_telemetry (shift_id, recorded_at);
