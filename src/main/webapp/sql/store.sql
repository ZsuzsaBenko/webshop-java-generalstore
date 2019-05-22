drop table if exists product;
drop table if exists product_category;
drop table if exists supplier;

create table supplier
(
    id serial not null primary key,
    name varchar not null,
    description varchar not null
);

create table product_category
(
    id serial not null primary key,
    name varchar not null,
    department varchar not null,
    description varchar not null
);

create table product
(
    id serial not null primary key,
    name varchar not null,
    default_price numeric(12,2) not null,
    currency varchar not null,
    description varchar not null,
    supplier integer references supplier(id),
    product_category integer not null references product_category(id)
);

create table users
(
    id serial not null primary key,
    name varchar not null,
    email varchar not null,
    password varchar not null
);
