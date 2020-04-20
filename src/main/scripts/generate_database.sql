create table users
(
    username      varchar(32)  not null
        constraint users_pk
            primary key,
    password_hash varchar(256) not null,
    password_salt varchar(256) not null
);

alter table users
    owner to javagram_admin;

create table have_relation
(
    sender   varchar(32)       not null
        constraint have_relation_fk_sender
            references users
            on update cascade on delete cascade,
    receiver varchar(32)       not null
        constraint have_relation_fk_receiver
            references users
            on update cascade on delete cascade,
    status   integer default 0 not null,
    constraint have_relation_pk
        primary key (receiver, sender)
);

alter table have_relation
    owner to javagram_admin;

