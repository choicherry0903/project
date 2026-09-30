-- Older staff accounts have no registered phone number; new registrations provide one.
ALTER TABLE staff_accounts ADD COLUMN phone varchar(15);
CREATE INDEX ix_staff_name_phone ON staff_accounts(company_id, display_name, phone);
