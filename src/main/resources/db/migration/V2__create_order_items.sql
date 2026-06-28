CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders (id),
    product_id VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(19, 2) NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
