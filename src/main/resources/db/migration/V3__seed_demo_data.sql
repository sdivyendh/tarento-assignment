INSERT INTO Store(id, name, quantityProductsInStock)
VALUES (1, 'TONSTAD', 10);
INSERT INTO Store(id, name, quantityProductsInStock)
VALUES (2, 'KALLAX', 5);
INSERT INTO Store(id, name, quantityProductsInStock)
VALUES (3, 'BESTÅ', 3);
ALTER SEQUENCE Store_SEQ RESTART WITH 4;

INSERT INTO Product(id, name, stock)
VALUES (1, 'TONSTAD', 10);
INSERT INTO Product(id, name, stock)
VALUES (2, 'KALLAX', 5);
INSERT INTO Product(id, name, stock)
VALUES (3, 'BESTÅ', 3);
ALTER SEQUENCE Product_SEQ RESTART WITH 4;

INSERT INTO warehouse(
    id,
    businessUnitCode,
    activeBusinessUnitCode,
    location,
    capacity,
    stock,
    createdAt,
    archivedAt
)
VALUES (1, 'MWH.001', 'MWH.001', 'ZWOLLE-001', 40, 10, '2024-07-01', NULL);
INSERT INTO warehouse(
    id,
    businessUnitCode,
    activeBusinessUnitCode,
    location,
    capacity,
    stock,
    createdAt,
    archivedAt
)
VALUES (2, 'MWH.012', 'MWH.012', 'AMSTERDAM-001', 50, 5, '2023-07-01', NULL);
INSERT INTO warehouse(
    id,
    businessUnitCode,
    activeBusinessUnitCode,
    location,
    capacity,
    stock,
    createdAt,
    archivedAt
)
VALUES (3, 'MWH.023', 'MWH.023', 'TILBURG-001', 30, 27, '2021-02-01', NULL);
ALTER SEQUENCE warehouse_SEQ RESTART WITH 4;
