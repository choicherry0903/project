ALTER TABLE materials ADD COLUMN image_key varchar(512);

-- Cleanup intents survive a failed upload/DB transaction and can be retried.
CREATE TABLE storage_deletions (
  object_key varchar(512) PRIMARY KEY,
  available_at timestamp(6) with time zone NOT NULL,
  attempts integer NOT NULL DEFAULT 0
);
