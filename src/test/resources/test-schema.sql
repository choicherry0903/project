-- 조회에서 사용하는 기존 테이블 컬럼만 재현한 테스트 전용 스키마.
-- 실제 Supabase DDL은 실행/변경하지 않는다.
CREATE SCHEMA IF NOT EXISTS interior_cms;
CREATE TABLE IF NOT EXISTS interior_cms.customers (
    id BIGINT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    archived BOOLEAN NOT NULL
);
CREATE TABLE IF NOT EXISTS interior_cms.construction_projects (
    id BIGINT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(200) NOT NULL,
    detail_address VARCHAR(200),
    status VARCHAR(30) NOT NULL
);
CREATE TABLE IF NOT EXISTS interior_cms.staff_accounts (
    id BIGINT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    username VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    display_name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL,
    email VARCHAR(254),
    email_verified_at TIMESTAMP WITH TIME ZONE
);
