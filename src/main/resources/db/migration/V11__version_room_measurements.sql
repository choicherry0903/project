ALTER TABLE room_measurements ADD COLUMN room_key varchar(64);

-- Previously stored measurements become independent first revisions.
UPDATE room_measurements SET room_key = CONCAT('legacy-', CAST(id AS varchar(20)));

ALTER TABLE room_measurements ALTER COLUMN room_key SET NOT NULL;
CREATE UNIQUE INDEX uq_room_measurement_revision
  ON room_measurements (company_id, project_id, room_key, revision);
