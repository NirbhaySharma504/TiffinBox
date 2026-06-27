-- Runs once on first Postgres startup (mounted into /docker-entrypoint-initdb.d).
-- Creates one database per service, owned by the default POSTGRES_USER.
CREATE DATABASE userdb;
CREATE DATABASE menudb;
CREATE DATABASE orderdb;
CREATE DATABASE paymentdb;
CREATE DATABASE notifdb;
CREATE DATABASE subscriptiondb;
