create table author
(
    id         serial primary key,
    fio        text  not null,
    timestamp  timestamp  not null
);