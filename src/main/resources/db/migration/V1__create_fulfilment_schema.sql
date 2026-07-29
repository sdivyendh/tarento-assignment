CREATE SEQUENCE Product_SEQ START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE Store_SEQ START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE warehouse_SEQ START WITH 1 INCREMENT BY 50;

CREATE TABLE Product (
    price NUMERIC(10, 2),
    stock INTEGER NOT NULL,
    id BIGINT NOT NULL,
    name VARCHAR(40) UNIQUE,
    description VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE Store (
    quantityProductsInStock INTEGER NOT NULL,
    id BIGINT NOT NULL,
    name VARCHAR(40) UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE warehouse (
    capacity INTEGER NOT NULL,
    stock INTEGER NOT NULL,
    archivedAt TIMESTAMP,
    createdAt TIMESTAMP NOT NULL,
    id BIGINT NOT NULL,
    activeBusinessUnitCode VARCHAR(40) UNIQUE,
    businessUnitCode VARCHAR(40) NOT NULL,
    location VARCHAR(40) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE warehouse_location_lock (
    identifier VARCHAR(40) NOT NULL,
    PRIMARY KEY (identifier)
);

INSERT INTO warehouse_location_lock(identifier) VALUES ('ZWOLLE-001');
INSERT INTO warehouse_location_lock(identifier) VALUES ('ZWOLLE-002');
INSERT INTO warehouse_location_lock(identifier) VALUES ('AMSTERDAM-001');
INSERT INTO warehouse_location_lock(identifier) VALUES ('AMSTERDAM-002');
INSERT INTO warehouse_location_lock(identifier) VALUES ('TILBURG-001');
INSERT INTO warehouse_location_lock(identifier) VALUES ('HELMOND-001');
INSERT INTO warehouse_location_lock(identifier) VALUES ('EINDHOVEN-001');
INSERT INTO warehouse_location_lock(identifier) VALUES ('VETSBY-001');
