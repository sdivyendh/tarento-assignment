CREATE SEQUENCE fulfilment_assignment_SEQ START WITH 1 INCREMENT BY 50;

CREATE TABLE fulfilment_assignment (
    id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_fulfilment_store_product_warehouse
        UNIQUE (store_id, product_id, warehouse_id),
    CONSTRAINT fk_fulfilment_store
        FOREIGN KEY (store_id) REFERENCES Store(id),
    CONSTRAINT fk_fulfilment_product
        FOREIGN KEY (product_id) REFERENCES Product(id),
    CONSTRAINT fk_fulfilment_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouse(id)
);

CREATE INDEX idx_fulfilment_store ON fulfilment_assignment(store_id);
CREATE INDEX idx_fulfilment_product ON fulfilment_assignment(product_id);
CREATE INDEX idx_fulfilment_warehouse ON fulfilment_assignment(warehouse_id);
