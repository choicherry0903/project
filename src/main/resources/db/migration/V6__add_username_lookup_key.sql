ALTER TABLE username_lookup_attempts ADD COLUMN lookup_key varchar(64);

CREATE INDEX ix_username_lookup_target_failures
  ON username_lookup_attempts(company_id, lookup_key, outcome, created_at);
