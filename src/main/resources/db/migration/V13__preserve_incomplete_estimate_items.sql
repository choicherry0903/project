ALTER TABLE estimate_items ADD COLUMN quantity_missing boolean NOT NULL DEFAULT false;
ALTER TABLE estimate_items ADD COLUMN unit_price_missing boolean NOT NULL DEFAULT false;
