--Ben Marksberry
--CS4322
--"Duluth Fine Bakery" Database Final Project

alter table item
    drop constraint item_recipe_id_fk;

alter table customer
    drop constraint customer_credit_card_card_num_fk;

alter table customer
    drop constraint customer_paypal_username_fk;

drop table credit_card;

drop table paypal;

drop table recipe_ingredients;

drop table recipe;

drop table chef_baked_items;

drop table chef;

drop table ingredient_suppliers;

drop table ingredient;

drop table supplier;

drop table order_items;

drop table item;

drop table order_payments;

drop table "order";

drop table customer;

