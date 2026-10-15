package com.roofandwall.interior.customer.repository;

import com.roofandwall.interior.customer.dto.CustomerListItem;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

/** 기존 Supabase 테이블을 조회한다. 데이터/스키마 변경 쿼리는 포함하지 않는다. */
@Repository
public class CustomerRepository {

    private final JdbcClient jdbcClient;


    public CustomerRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;

    }

    public List<CustomerListItem> findAll(long companyId) {
        return jdbcClient.sql("""
                SELECT p.id, p.name AS project_name,
                       c.name AS customer_name, c.phone AS customer_phone,
                       TRIM(CONCAT(p.address, ' ', COALESCE(p.detail_address, ''))) AS site_address,
                       p.status
                FROM interior_cms.construction_projects p
                JOIN interior_cms.customers c
                  ON c.id = p.customer_id AND c.company_id = p.company_id
                WHERE p.company_id = :companyId AND c.archived = FALSE
                ORDER BY p.id DESC
                """)
                .param("companyId", companyId)
                .query((rs, rowNum) -> new CustomerListItem(
                        rs.getLong("id"),
                        rs.getString("project_name"),
                        rs.getString("customer_name"),
                        rs.getString("customer_phone"),
                        rs.getString("site_address"),
                        rs.getString("status")
                ))
                .list();
    }
}
