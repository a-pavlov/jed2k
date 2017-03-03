create table test.sources (
  kad_id character(16) not null
  , host inet not null
  , port_tcp int not null default 0
  , port_udp int not null default 0
  , packet bytea not null
  , last_update timestamp not null default current_timestamp
  , source_type character(1) not null
  , constraint sources_pk primary key (kad_id, host, port_tcp, port_udp)
);

create index sources_update_indx on test.sources(last_update);

create table test.keywords (
  kad_id character(16)
  , file_id character(16)
  , host inet not null
  , packet bytea not null
  , last_update timestamp not null default current_timestamp
  , constraint keywords_pk primary key(kad_id, file_id, host)
);

create index keywords_update_indx on test.keywords(last_update);
