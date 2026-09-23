-- Initial schema for MySQL 8.4. Flyway applies this once after database creation.
-- All timestamps are UTC; all monetary amounts are integer KRW.

CREATE TABLE staff_accounts (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id bigint NOT NULL,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamp(6) NOT NULL,
  updated_at timestamp(6) NOT NULL,
  email varchar(254) NOT NULL UNIQUE,
  password_hash varchar(100) NOT NULL,
  display_name varchar(50) NOT NULL,
  role varchar(20) NOT NULL,
  active boolean NOT NULL,
  CONSTRAINT uq_staff_accounts_tenant UNIQUE (company_id, id)
);

CREATE TABLE customers (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id bigint NOT NULL,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamp(6) NOT NULL,
  updated_at timestamp(6) NOT NULL,
  name varchar(50) NOT NULL,
  phone varchar(15) NOT NULL,
  source varchar(100),
  memo varchar(2000),
  request_key varchar(36) NOT NULL UNIQUE,
  manager_id bigint,
  archived boolean NOT NULL,
  CONSTRAINT uq_customers_tenant UNIQUE (company_id, id),
  CONSTRAINT fk_customers_manager_id FOREIGN KEY (company_id, manager_id) REFERENCES staff_accounts (company_id, id)
);

CREATE TABLE consultations (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id bigint NOT NULL,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamp(6) NOT NULL,
  updated_at timestamp(6) NOT NULL,
  customer_id bigint NOT NULL,
  author_id bigint NOT NULL,
  content varchar(2000) NOT NULL,
  CONSTRAINT uq_consultations_tenant UNIQUE (company_id, id),
  CONSTRAINT fk_consultations_customer_id FOREIGN KEY (company_id, customer_id) REFERENCES customers (company_id, id),
  CONSTRAINT fk_consultations_author_id FOREIGN KEY (company_id, author_id) REFERENCES staff_accounts (company_id, id)
);

CREATE TABLE construction_projects (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id bigint NOT NULL,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamp(6) NOT NULL,
  updated_at timestamp(6) NOT NULL,
  customer_id bigint NOT NULL,
  name varchar(100) NOT NULL,
  address varchar(200) NOT NULL,
  detail_address varchar(200),
  construction_type varchar(50) NOT NULL,
  desired_start_date date,
  housing_type varchar(50),
  occupied boolean NOT NULL,
  site_notes varchar(2000),
  status varchar(30) NOT NULL,
  on_hold boolean NOT NULL,
  hold_reason varchar(1000),
  request_key varchar(36) NOT NULL UNIQUE,
  CONSTRAINT uq_construction_projects_tenant UNIQUE (company_id, id),
  CONSTRAINT fk_construction_projects_customer_id FOREIGN KEY (company_id, customer_id) REFERENCES customers (company_id, id)
);

CREATE TABLE room_measurements (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id bigint NOT NULL,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamp(6) NOT NULL,
  updated_at timestamp(6) NOT NULL,
  project_id bigint NOT NULL,
  surveyor_id bigint NOT NULL,
  revision integer NOT NULL,
  room_name varchar(100) NOT NULL,
  perimeter decimal(12,2) NOT NULL,
  height decimal(12,2) NOT NULL,
  opening_area decimal(12,2) NOT NULL,
  ceiling_area decimal(12,2) NOT NULL,
  include_ceiling boolean NOT NULL,
  surface_condition varchar(1000),
  CONSTRAINT uq_room_measurements_tenant UNIQUE (company_id, id),
  CONSTRAINT fk_room_measurements_project_id FOREIGN KEY (company_id, project_id) REFERENCES construction_projects (company_id, id),
  CONSTRAINT fk_room_measurements_surveyor_id FOREIGN KEY (company_id, surveyor_id) REFERENCES staff_accounts (company_id, id),
  CONSTRAINT ck_measurement CHECK (perimeter > 0 AND height > 0 AND opening_area >= 0 AND ceiling_area >= 0 AND perimeter * height >= opening_area)
);

CREATE TABLE estimates (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id bigint NOT NULL,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamp(6) NOT NULL,
  updated_at timestamp(6) NOT NULL,
  project_id bigint NOT NULL,
  revision integer NOT NULL,
  status varchar(30) NOT NULL,
  valid_until date,
  scope_of_work varchar(2000),
  excluded_work varchar(2000),
  revision_reason varchar(1000),
  discount bigint NOT NULL,
  tax_rate decimal(5,4) NOT NULL,
  supply_amount bigint NOT NULL,
  tax_amount bigint NOT NULL,
  total_amount bigint NOT NULL,
  company_snapshot varchar(2000),
  confirmed_at timestamp(6),
  CONSTRAINT uq_estimates_tenant UNIQUE (company_id, id),
  CONSTRAINT uq_estimate_revision UNIQUE (company_id, project_id, revision),
  CONSTRAINT fk_estimates_project_id FOREIGN KEY (company_id, project_id) REFERENCES construction_projects (company_id, id)
);

CREATE TABLE estimate_items (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id bigint NOT NULL,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamp(6) NOT NULL,
  updated_at timestamp(6) NOT NULL,
  estimate_id bigint NOT NULL,
  category varchar(30) NOT NULL,
  name varchar(200) NOT NULL,
  unit varchar(20) NOT NULL,
  quantity decimal(12,2) NOT NULL,
  unit_price bigint NOT NULL,
  amount bigint NOT NULL,
  free_item_reason varchar(500),
  CONSTRAINT uq_estimate_items_tenant UNIQUE (company_id, id),
  CONSTRAINT fk_estimate_items_estimate_id FOREIGN KEY (company_id, estimate_id) REFERENCES estimates (company_id, id)
);

CREATE TABLE construction_contracts (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id bigint NOT NULL,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamp(6) NOT NULL,
  updated_at timestamp(6) NOT NULL,
  project_id bigint NOT NULL UNIQUE,
  estimate_id bigint NOT NULL,
  consented_at timestamp(6) NOT NULL,
  consent_method varchar(200) NOT NULL,
  scope_snapshot varchar(2000) NOT NULL,
  total_amount bigint NOT NULL,
  special_terms varchar(2000),
  CONSTRAINT uq_construction_contracts_tenant UNIQUE (company_id, id),
  CONSTRAINT fk_construction_contracts_project_id FOREIGN KEY (company_id, project_id) REFERENCES construction_projects (company_id, id),
  CONSTRAINT fk_construction_contracts_estimate_id FOREIGN KEY (company_id, estimate_id) REFERENCES estimates (company_id, id)
);

CREATE TABLE contract_changes (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id bigint NOT NULL,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamp(6) NOT NULL,
  updated_at timestamp(6) NOT NULL,
  contract_id bigint NOT NULL,
  estimate_id bigint NOT NULL,
  delta_amount bigint NOT NULL,
  reason varchar(1000) NOT NULL,
  consented_at timestamp(6) NOT NULL,
  CONSTRAINT uq_contract_changes_tenant UNIQUE (company_id, id),
  CONSTRAINT fk_contract_changes_contract_id FOREIGN KEY (company_id, contract_id) REFERENCES construction_contracts (company_id, id),
  CONSTRAINT fk_contract_changes_estimate_id FOREIGN KEY (company_id, estimate_id) REFERENCES estimates (company_id, id)
);

CREATE TABLE payment_plans (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id bigint NOT NULL,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamp(6) NOT NULL,
  updated_at timestamp(6) NOT NULL,
  contract_id bigint NOT NULL,
  name varchar(100) NOT NULL,
  due_date date NOT NULL,
  amount bigint NOT NULL,
  CONSTRAINT uq_payment_plans_tenant UNIQUE (company_id, id),
  CONSTRAINT fk_payment_plans_contract_id FOREIGN KEY (company_id, contract_id) REFERENCES construction_contracts (company_id, id)
);

CREATE TABLE payment_transactions (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id bigint NOT NULL,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamp(6) NOT NULL,
  updated_at timestamp(6) NOT NULL,
  contract_id bigint NOT NULL,
  plan_id bigint NOT NULL,
  original_transaction_id bigint,
  type varchar(30) NOT NULL,
  transaction_date date NOT NULL,
  amount bigint NOT NULL,
  method varchar(30) NOT NULL,
  reason varchar(1000),
  request_key varchar(36) NOT NULL UNIQUE,
  CONSTRAINT uq_payment_transactions_tenant UNIQUE (company_id, id),
  CONSTRAINT fk_payment_transactions_contract_id FOREIGN KEY (company_id, contract_id) REFERENCES construction_contracts (company_id, id),
  CONSTRAINT fk_payment_transactions_plan_id FOREIGN KEY (company_id, plan_id) REFERENCES payment_plans (company_id, id),
  CONSTRAINT fk_payment_transactions_original_transaction_id FOREIGN KEY (company_id, original_transaction_id) REFERENCES payment_transactions (company_id, id)
);

CREATE TABLE work_schedules (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id bigint NOT NULL,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamp(6) NOT NULL,
  updated_at timestamp(6) NOT NULL,
  project_id bigint NOT NULL,
  type varchar(30) NOT NULL,
  starts_at timestamp(6) NOT NULL,
  ends_at timestamp(6) NOT NULL,
  status varchar(30) NOT NULL,
  notes varchar(1000),
  CONSTRAINT uq_work_schedules_tenant UNIQUE (company_id, id),
  CONSTRAINT fk_work_schedules_project_id FOREIGN KEY (company_id, project_id) REFERENCES construction_projects (company_id, id),
  CONSTRAINT ck_schedule_time CHECK (ends_at > starts_at)
);

CREATE TABLE worker_assignments (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id bigint NOT NULL,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamp(6) NOT NULL,
  updated_at timestamp(6) NOT NULL,
  schedule_id bigint NOT NULL,
  worker_id bigint NOT NULL,
  team_leader boolean NOT NULL,
  CONSTRAINT uq_worker_assignments_tenant UNIQUE (company_id, id),
  CONSTRAINT uq_worker_assignment UNIQUE (company_id, schedule_id, worker_id),
  CONSTRAINT fk_worker_assignments_schedule_id FOREIGN KEY (company_id, schedule_id) REFERENCES work_schedules (company_id, id),
  CONSTRAINT fk_worker_assignments_worker_id FOREIGN KEY (company_id, worker_id) REFERENCES staff_accounts (company_id, id)
);

CREATE TABLE work_logs (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id bigint NOT NULL,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamp(6) NOT NULL,
  updated_at timestamp(6) NOT NULL,
  schedule_id bigint NOT NULL,
  author_id bigint NOT NULL,
  work_date date NOT NULL,
  content varchar(2000) NOT NULL,
  issue varchar(1000),
  resolved boolean NOT NULL,
  CONSTRAINT uq_work_logs_tenant UNIQUE (company_id, id),
  CONSTRAINT fk_work_logs_schedule_id FOREIGN KEY (company_id, schedule_id) REFERENCES work_schedules (company_id, id),
  CONSTRAINT fk_work_logs_author_id FOREIGN KEY (company_id, author_id) REFERENCES staff_accounts (company_id, id)
);

CREATE TABLE completion_inspections (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id bigint NOT NULL,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamp(6) NOT NULL,
  updated_at timestamp(6) NOT NULL,
  project_id bigint NOT NULL,
  requester_id bigint NOT NULL,
  approver_id bigint,
  scope_checked boolean NOT NULL,
  finish_checked boolean NOT NULL,
  cleanup_checked boolean NOT NULL,
  approved_at timestamp(6),
  feedback varchar(1000),
  CONSTRAINT uq_completion_inspections_tenant UNIQUE (company_id, id),
  CONSTRAINT fk_completion_inspections_project_id FOREIGN KEY (company_id, project_id) REFERENCES construction_projects (company_id, id),
  CONSTRAINT fk_completion_inspections_requester_id FOREIGN KEY (company_id, requester_id) REFERENCES staff_accounts (company_id, id),
  CONSTRAINT fk_completion_inspections_approver_id FOREIGN KEY (company_id, approver_id) REFERENCES staff_accounts (company_id, id)
);

CREATE TABLE materials (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id bigint NOT NULL,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamp(6) NOT NULL,
  updated_at timestamp(6) NOT NULL,
  category varchar(50) NOT NULL,
  brand varchar(100),
  name varchar(200) NOT NULL,
  product_code varchar(100),
  color varchar(100),
  unit varchar(20) NOT NULL,
  selling_price bigint NOT NULL,
  coverage_per_roll decimal(12,2),
  waste_rate decimal(5,4),
  active boolean NOT NULL,
  CONSTRAINT uq_materials_tenant UNIQUE (company_id, id)
);

CREATE TABLE purchase_orders (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id bigint NOT NULL,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamp(6) NOT NULL,
  updated_at timestamp(6) NOT NULL,
  project_id bigint NOT NULL,
  supplier_name varchar(200) NOT NULL,
  expected_date date NOT NULL,
  status varchar(30) NOT NULL,
  CONSTRAINT uq_purchase_orders_tenant UNIQUE (company_id, id),
  CONSTRAINT fk_purchase_orders_project_id FOREIGN KEY (company_id, project_id) REFERENCES construction_projects (company_id, id)
);

CREATE TABLE purchase_order_items (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id bigint NOT NULL,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamp(6) NOT NULL,
  updated_at timestamp(6) NOT NULL,
  purchase_order_id bigint NOT NULL,
  material_id bigint NOT NULL,
  quantity decimal(12,2) NOT NULL,
  received_quantity decimal(12,2) NOT NULL,
  unit_cost bigint NOT NULL,
  CONSTRAINT uq_purchase_order_items_tenant UNIQUE (company_id, id),
  CONSTRAINT fk_purchase_order_items_purchase_order_id FOREIGN KEY (company_id, purchase_order_id) REFERENCES purchase_orders (company_id, id),
  CONSTRAINT fk_purchase_order_items_material_id FOREIGN KEY (company_id, material_id) REFERENCES materials (company_id, id)
);

CREATE TABLE project_costs (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id bigint NOT NULL,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamp(6) NOT NULL,
  updated_at timestamp(6) NOT NULL,
  project_id bigint NOT NULL,
  incurred_date date NOT NULL,
  category varchar(30) NOT NULL,
  supply_amount bigint NOT NULL,
  payment_status varchar(30) NOT NULL,
  source_key varchar(100) UNIQUE,
  memo varchar(1000),
  CONSTRAINT uq_project_costs_tenant UNIQUE (company_id, id),
  CONSTRAINT fk_project_costs_project_id FOREIGN KEY (company_id, project_id) REFERENCES construction_projects (company_id, id)
);

CREATE TABLE after_service_requests (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id bigint NOT NULL,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamp(6) NOT NULL,
  updated_at timestamp(6) NOT NULL,
  project_id bigint NOT NULL,
  previous_request_id bigint,
  received_date date NOT NULL,
  symptom varchar(2000) NOT NULL,
  urgency varchar(20) NOT NULL,
  status varchar(30) NOT NULL,
  resolution varchar(2000),
  CONSTRAINT uq_after_service_requests_tenant UNIQUE (company_id, id),
  CONSTRAINT fk_after_service_requests_project_id FOREIGN KEY (company_id, project_id) REFERENCES construction_projects (company_id, id),
  CONSTRAINT fk_after_service_requests_previous_request_id FOREIGN KEY (company_id, previous_request_id) REFERENCES after_service_requests (company_id, id)
);

CREATE TABLE notifications (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id bigint NOT NULL,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamp(6) NOT NULL,
  updated_at timestamp(6) NOT NULL,
  recipient_id bigint NOT NULL,
  event_key varchar(200) NOT NULL UNIQUE,
  message varchar(500) NOT NULL,
  target_path varchar(500) NOT NULL,
  read_at timestamp(6),
  CONSTRAINT uq_notifications_tenant UNIQUE (company_id, id),
  CONSTRAINT fk_notifications_recipient_id FOREIGN KEY (company_id, recipient_id) REFERENCES staff_accounts (company_id, id)
);

CREATE TABLE attachments (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id bigint NOT NULL,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamp(6) NOT NULL,
  updated_at timestamp(6) NOT NULL,
  project_id bigint NOT NULL,
  uploader_id bigint NOT NULL,
  record_type varchar(30) NOT NULL,
  record_id bigint NOT NULL,
  storage_key varchar(200) NOT NULL UNIQUE,
  original_filename varchar(255) NOT NULL,
  content_type varchar(100) NOT NULL,
  size_bytes bigint NOT NULL,
  CONSTRAINT uq_attachments_tenant UNIQUE (company_id, id),
  CONSTRAINT fk_attachments_project_id FOREIGN KEY (company_id, project_id) REFERENCES construction_projects (company_id, id),
  CONSTRAINT fk_attachments_uploader_id FOREIGN KEY (company_id, uploader_id) REFERENCES staff_accounts (company_id, id)
);

CREATE TABLE audit_events (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  company_id bigint NOT NULL,
  version bigint NOT NULL DEFAULT 0,
  created_at timestamp(6) NOT NULL,
  updated_at timestamp(6) NOT NULL,
  actor_id bigint NOT NULL,
  record_type varchar(30) NOT NULL,
  record_id bigint NOT NULL,
  action varchar(50) NOT NULL,
  before_value varchar(4000),
  after_value varchar(4000),
  reason varchar(1000),
  CONSTRAINT uq_audit_events_tenant UNIQUE (company_id, id),
  CONSTRAINT fk_audit_events_actor_id FOREIGN KEY (company_id, actor_id) REFERENCES staff_accounts (company_id, id)
);

CREATE INDEX ix_customer_phone ON customers(company_id, phone);
CREATE INDEX ix_project_updated ON construction_projects(company_id, updated_at);
CREATE INDEX ix_schedule_period ON work_schedules(company_id, starts_at, ends_at);
CREATE INDEX ix_notifications_unread ON notifications(company_id, recipient_id, read_at);
