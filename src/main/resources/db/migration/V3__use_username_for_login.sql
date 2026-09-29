-- Preserve existing login identifiers and password hashes.
-- Existing email-shaped identifiers remain valid usernames until explicitly changed.
ALTER TABLE staff_accounts RENAME COLUMN email TO username;
