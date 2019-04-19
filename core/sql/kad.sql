drop table kad.sources;
drop table kad.keywords;
create schema kad;

create table kad.sources (
  kad_id character(32) not null
  , host inet not null
  , port_tcp int not null default 0
  , port_udp int not null default 0
  , packet bytea not null
  , last_update timestamp not null default current_timestamp
  , total_updates int not null default 0
  , source_type int not null
  , constraint sources_pk primary key (kad_id, host, port_tcp, port_udp)
);

create index sources_update_indx on kad.sources(last_update);

create table kad.keywords (
  kad_id character(32)
  , file_id character(32)
  , host inet not null
  , packet bytea not null
  , last_update timestamp not null default current_timestamp
  , total_updates int not null default 0
  , constraint keywords_pk primary key(kad_id, file_id, host)
);

create index keywords_update_indx on kad.keywords(last_update);

create table kad.search_statistics (
    id bigserial not null
    , kad_id character(32) not null
    , ts timestamp not null default current_timestamp
    , res_count int not null
    , type varchar(2)
    , constraint ss_pk primary key(id)
);

create index ss_kad_indx on kad.search_statistics(kad_id);
create index ss_res_count_indx on kad.search_statistics(res_count);

grant usage on schema kad to kad;
grant all on table kad.sources to kad;
grant all on table kad.keywords to kad;
grant all on table kad.search_statistics to kad;
grant usage, select on sequence kad.search_statistics_id_seq to kad;
