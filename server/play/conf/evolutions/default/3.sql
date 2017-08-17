# --- Database schema

# --- !Ups

create table messages (
  id                       int auto_increment primary key,
  tech_id                  int,
  log_time                 datetime,
  code                     smallint,
  tag                      varchar(32),
  messages                 text,
  trace                    text
);

# --- !Downs

drop table if exists messages;
