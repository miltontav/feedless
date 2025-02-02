DELETE FROM t_order;
ALTER TABLE t_order ADD COLUMN stripe_status varchar(100);
ALTER TABLE t_order ADD COLUMN stripe_session_id varchar(100);
ALTER TABLE t_order ADD COLUMN stripe_subscription_id varchar(100);
ALTER TABLE t_order ADD COLUMN stripe_last_synced_at timestamp(6) without time zone;

ALTER TABLE t_priced_product ADD COLUMN iteration integer;

ALTER TABLE t_user ADD COLUMN stripe_customer_id varchar(100);
ALTER TABLE t_product ADD COLUMN stripe_product_id varchar(100);

ALTER TABLE t_user RENAME COLUMN first_name TO name;
ALTER TABLE t_user DROP COLUMN last_name;

DELETE FROM t_order;
ALTER TABLE t_order DROP COLUMN is_paid;
ALTER TABLE t_order DROP COLUMN is_rejected;
ALTER TABLE t_order DROP COLUMN is_offer;
ALTER TABLE t_order ADD COLUMN paid_from timestamp(6) without time zone;
ALTER TABLE t_order ADD COLUMN paid_until timestamp(6) without time zone;
ALTER TABLE t_order ADD COLUMN status character varying(50) NOT NULL;

ALTER TABLE t_invoice DROP COLUMN price;
ALTER TABLE t_invoice DROP COLUMN is_canceled;
ALTER TABLE t_invoice DROP COLUMN due_to;
ALTER TABLE t_invoice DROP COLUMN paid_at;
ALTER TABLE t_invoice ADD COLUMN amount_paid bigint not null;
ALTER TABLE t_invoice ADD COLUMN amount_remaining bigint not null;
ALTER TABLE t_invoice ADD COLUMN pdf_url varchar(256) not null;
ALTER TABLE t_invoice ADD COLUMN invoice_id varchar(100) not null;
ALTER TABLE t_invoice ADD COLUMN customer_email varchar(100) not null;
