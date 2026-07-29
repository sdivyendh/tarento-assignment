INSERT INTO Store(id, name, quantityProductsInStock)
SELECT nextval('store_seq'), 'TONSTAD', 10
WHERE NOT EXISTS (SELECT 1 FROM Store WHERE name = 'TONSTAD');

INSERT INTO Store(id, name, quantityProductsInStock)
SELECT nextval('store_seq'), 'KALLAX', 5
WHERE NOT EXISTS (SELECT 1 FROM Store WHERE name = 'KALLAX');

INSERT INTO Store(id, name, quantityProductsInStock)
SELECT nextval('store_seq'), 'BESTÅ', 3
WHERE NOT EXISTS (SELECT 1 FROM Store WHERE name = 'BESTÅ');

INSERT INTO Product(id, name, stock)
SELECT nextval('product_seq'), 'TONSTAD', 10
WHERE NOT EXISTS (SELECT 1 FROM Product WHERE name = 'TONSTAD');

INSERT INTO Product(id, name, stock)
SELECT nextval('product_seq'), 'KALLAX', 5
WHERE NOT EXISTS (SELECT 1 FROM Product WHERE name = 'KALLAX');

INSERT INTO Product(id, name, stock)
SELECT nextval('product_seq'), 'BESTÅ', 3
WHERE NOT EXISTS (SELECT 1 FROM Product WHERE name = 'BESTÅ');

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
SELECT
    nextval('warehouse_seq'),
    'MWH.001',
    'MWH.001',
    'ZWOLLE-001',
    40,
    10,
    '2024-07-01',
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM warehouse WHERE businessUnitCode = 'MWH.001'
);

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
SELECT
    nextval('warehouse_seq'),
    'MWH.012',
    'MWH.012',
    'AMSTERDAM-001',
    50,
    5,
    '2023-07-01',
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM warehouse WHERE businessUnitCode = 'MWH.012'
);

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
SELECT
    nextval('warehouse_seq'),
    'MWH.023',
    'MWH.023',
    'TILBURG-001',
    30,
    27,
    '2021-02-01',
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM warehouse WHERE businessUnitCode = 'MWH.023'
);
