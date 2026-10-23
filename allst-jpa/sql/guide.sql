-- 指南场景表。spring.jpa.hibernate.ddl-auto=update 时可以不手工执行。
-- 旧练习表 person / users 不在这里重建。

create table if not exists g_department (
    id bigint primary key auto_increment,
    name varchar(64) not null unique,
    created_at datetime null,
    updated_at datetime null,
    created_by varchar(64) null,
    last_modified_by varchar(64) null
);

create table if not exists g_employee (
    id bigint primary key auto_increment,
    emp_name varchar(64) not null,
    email varchar(128) not null,
    age int null,
    salary_amount decimal(19, 2) null,
    status varchar(16) not null,
    department_id bigint null,
    version bigint null,
    created_at datetime null,
    updated_at datetime null,
    created_by varchar(64) null,
    last_modified_by varchar(64) null,
    unique key idx_g_employee_email (email)
);

create table if not exists g_employee_profile (
    id bigint primary key auto_increment,
    phone varchar(32) null,
    address varchar(255) null,
    employee_id bigint not null unique
);

create table if not exists g_project (
    id bigint primary key auto_increment,
    code varchar(32) not null unique,
    title varchar(128) not null
);

create table if not exists g_employee_project (
    employee_id bigint not null,
    project_id bigint not null,
    primary key (employee_id, project_id)
);

create table if not exists g_customer (
    id bigint primary key auto_increment,
    name varchar(64) not null,
    password varchar(128) not null,
    birthday date null,
    nickname varchar(64) null,
    phone varchar(32) null
);

create table if not exists g_account (
    id bigint primary key auto_increment,
    owner varchar(64) not null,
    balance decimal(19, 2) not null,
    version bigint null
);

create table if not exists g_product (
    id bigint primary key auto_increment,
    name varchar(128) not null,
    price decimal(19, 2) not null,
    product_type varchar(31) not null
);

create table if not exists g_book (
    id bigint primary key,
    isbn varchar(32) null,
    author varchar(64) null
);

create table if not exists g_digital_goods (
    id bigint primary key,
    download_url varchar(255) null,
    license_days int null
);

create table if not exists g_customer_order (
    id bigint primary key auto_increment,
    order_no varchar(32) not null unique,
    customer_name varchar(64) not null
);

create table if not exists g_order_item (
    id bigint primary key auto_increment,
    sku varchar(64) not null,
    quantity int not null,
    price decimal(19, 2) not null,
    order_id bigint not null
);

create table if not exists g_audit_trail (
    id bigint primary key auto_increment,
    entity_name varchar(64) not null,
    action varchar(32) not null,
    happened_at datetime not null
);
