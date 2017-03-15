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
  , source_flag character(1) not null
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

grant all on table kad.sources to kad;
grant all on table kad.keywords to kad;