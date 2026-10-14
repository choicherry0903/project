ALTER TABLE estimates ADD COLUMN request_key varchar(36);
ALTER TABLE estimates ADD COLUMN archived boolean NOT NULL DEFAULT false;
ALTER TABLE estimates ADD COLUMN draft_edit_sequence bigint NOT NULL DEFAULT 0;
CREATE UNIQUE INDEX uq_estimate_request_key ON estimates (company_id, request_key);
CREATE INDEX ix_estimate_draft_list ON estimates (company_id, archived, updated_at);
