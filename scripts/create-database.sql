-- Run manually using a MySQL administrator. Replace the password first.
CREATE DATABASE IF NOT EXISTS interior_cms
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER IF NOT EXISTS 'interior_app'@'localhost'
  IDENTIFIED BY 'CHANGE_ME_BEFORE_RUNNING';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES
  ON interior_cms.* TO 'interior_app'@'localhost';
-- Application startup applies Flyway migrations. No DROP or root login required.
