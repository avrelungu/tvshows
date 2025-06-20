CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE if not exists app_user
(
    id        UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    username  VARCHAR(255) NOT NULL UNIQUE,
    email  VARCHAR(255) NOT NULL UNIQUE,
    password  VARCHAR(255) NOT NULL,
    role      VARCHAR(255)      NOT NULL
);