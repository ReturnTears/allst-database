create table personBo
(
    id      bigint primary key auto_increment,
    name    varchar(255) not null,
    age     int          not null default 0 check (age >= 0),
    address varchar(255) not null
) engine=InnoDB;
select * from person;
insert into person (name, age, address) values ('张三', 18, '北京');
insert into person (name, age, address) values ('lisi', 18, '上海');
