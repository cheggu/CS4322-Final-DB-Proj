--Ben Marksberry
--CS4322
--"Duluth Fine Bakery" Database Final Project

--all indices are default btrees because there is no need for more complicated index datatypes in this simple example
--all primary keys are automatically indexed in postgresql

create table item
(
    name        varchar(48) not null
        constraint item_pk
            primary key,
    description varchar(200),
    cost        money       not null,
    recipe      varchar(16) not null
);

--index on name for faster lookup on item name, can be useful when querying recipes
create index item_name_index
    on item (name);

--index on cost for faster lookup on costs used for quicker invoice generation
create index item_cost_index
    on item (cost);

create table recipe
(
    id          varchar(32)  not null
        constraint recipe_pk
            primary key,
    description varchar(200) not null,
    name        varchar(32)  not null
        constraint recipe_item_name_fk
            references item
);

alter table item
    add constraint item_recipe_id_fk
        foreign key (recipe) references recipe;

create table ingredient
(
    name        varchar(64) not null
        constraint ingredient_pk
            primary key
        constraint ingredient_pk2
            unique,
    brand       varchar(48),
    amount_left integer     not null
);

--quickly look up ingredients by name
create index ingredient_name_index
    on ingredient (name);

create table supplier
(
    id         varchar(16) not null
        constraint supplier_pk
            primary key,
    first_name varchar(32) not null,
    last_name  varchar(32) not null,
    address    varchar(64) not null,
    phone      varchar(16) not null
        constraint supplier_pk2
            unique
);

--quickly look up suppliers and associated info
create index supplier_id_index
    on supplier (id);

create table chef
(
    id         varchar(8)  not null
        constraint chef_pk
            primary key,
    first_name varchar(32) not null,
    last_name  varchar(32) not null,
    phone      varchar(16) not null
);

create table customer
(
    id          varchar(16) not null
        constraint customer_pk
            primary key,
    first_name  varchar(32) not null,
    last_name   varchar(32) not null,
    email       varchar(64) not null
        constraint customer_pk3
            unique,
    phone       varchar(16) not null
        constraint customer_pk2
            unique,
    credit_card varchar(16),
    paypal      varchar(32)
);

create table "order"
(
    order_id             integer     not null
        constraint order_pk
            primary key,
    date_placed          date        not null,
    due_date             date,
    is_ready             boolean,
    special_instructions varchar(200),
    customer             varchar(16) not null
        constraint order_customer_id_fk
            references customer,
    constraint check_name
        check (due_date > date_placed)
);

--quickly retrieve orders for a specific customer
create index order_customer_index
    on "order" (customer);

--efficiently handle queries related to due dates.
create index order_due_date_index
    on "order" (due_date);

--quickly filter orders based on readiness.
create index order_is_ready_index
    on "order" (is_ready);

--quickly look up customers by email.
create index customer_email_index
    on customer (email);

--quickly look up customers by phone number.
create index customer_phone_index
    on customer (phone);

create table credit_card
(
    card_num   varchar(16) not null
        constraint credit_card_pk
            primary key
        constraint credit_card_pk2
            unique,
    expiration date        not null,
    cvv        varchar(3)  not null,
    owner      varchar(16) not null
        constraint credit_card_customer_id_fk
            references customer
);

alter table customer
    add constraint customer_credit_card_card_num_fk
        foreign key (credit_card) references credit_card;

create table paypal
(
    username varchar(32) not null
        constraint paypal_pk
            primary key
        constraint paypal_pk2
            unique,
    owner    varchar(16) not null
        constraint paypal_customer_id_fk
            references customer
);

alter table customer
    add constraint customer_paypal_username_fk
        foreign key (paypal) references paypal;

create table recipe_ingredients
(
    recipe     varchar(32) not null
        constraint recipe_ingredients_recipe_id_fk
            references recipe,
    ingredient varchar(64) not null
        constraint recipe_ingredients_ingredient_name_fk
            references ingredient,
    constraint recipe_ingredients_pk
        primary key (recipe, ingredient)
);

create table chef_baked_items
(
    chef    varchar(8)  not null
        constraint chef_baked_items_chef_id_fk
            references chef,
    item    varchar(48) not null
        constraint chef_baked_items_item_name_fk
            references item,
    "order" integer
        constraint chef_baked_items_order_order_id_fk
            references "order"
);

create table ingredient_suppliers
(
    ingredient varchar(64) not null
        constraint ingredient_suppliers_ingredient_name_fk
            references ingredient,
    supplier   varchar(64) not null
        constraint ingredient_suppliers_supplier_id_fk
            references supplier,
    constraint ingredient_suppliers_pk
        primary key (ingredient, supplier)
);

create table order_items
(
    "order"  integer     not null
        constraint order_items_order_order_id_fk
            references "order",
    item     varchar(48) not null
        constraint order_items_item_name_fk
            references item,
    quantity integer     not null,
    constraint order_items_pk
        primary key ("order", item)
);

create table order_payments
(
    "order" integer          not null
        constraint order_payments_order_order_id_fk
            references "order",
    type    varchar(6)       not null,
    amount  double precision not null
        constraint check_name
            check (amount >= (0)::double precision)
);

