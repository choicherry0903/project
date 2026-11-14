package com.roofandwall.interior.customer.repository;

import com.roofandwall.interior.customer.dto.CustomerItem;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.support.GeneratedKeyHolder;

/** 기존 Supabase 테이블의 고객 조회와 등록을 담당한다. */
@Repository
public class CustomerRepository {

    private final JdbcClient jdbcClient;


    public CustomerRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;

    }

    public List<CustomerItem> findAll(long companyId) {
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
                .query((rs, rowNum) -> new CustomerItem(
                        rs.getLong("id"),
                        rs.getString("project_name"),
                        rs.getString("customer_name"),
                        rs.getString("customer_phone"),
                        rs.getString("site_address"),
                        rs.getString("status")
                ))
                .list();
    }


    public Optional<CustomerItem> findDetail(long companyId, long projectId) {
        return jdbcClient.sql("""
                SELECT p.id, p.customer_id, p.version AS project_version, c.version AS customer_version, p.name AS project_name,
                       c.name AS customer_name, c.phone AS customer_phone,
                       TRIM(CONCAT(p.address, ' ', COALESCE(p.detail_address, ''))) AS site_address,
                       p.status
                FROM interior_cms.construction_projects p
                JOIN interior_cms.customers c ON c.id = p.customer_id AND c.company_id = p.company_id
                WHERE p.company_id = :companyId AND p.id = :projectId AND c.archived = FALSE
                """)
                .param("companyId", companyId).param("projectId", projectId)
                .query((rs, rowNum) -> {
                    var item = new CustomerItem(rs.getLong("id"), rs.getString("project_name"),
                            rs.getString("customer_name"), rs.getString("customer_phone"),
                            rs.getString("site_address"), rs.getString("status"));
                    item.setCustomerId(rs.getLong("customer_id"));
                    item.setCustomerVersion(rs.getLong("customer_version"));
                    item.setProjectVersion(rs.getLong("project_version"));
                    return item;
                }).optional();
    }

    public long insertCustomer(long companyId, CustomerItem form) {
        var keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql("""
                INSERT INTO interior_cms.customers
                    (company_id, version, created_at, updated_at, name, phone, request_key, archived)
                VALUES
                    (:companyId, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
                     :customerName, :customerPhone, :requestKey, FALSE)
                """)
                .param("companyId", companyId)
                .param("customerName", form.getCustomerName())
                .param("customerPhone", form.getCustomerPhone())
                .param("requestKey", form.getRequestKey())
                .update(keyHolder, "id");
        return keyHolder.getKey().longValue();
    }

    public void insertProject(long companyId, long customerId, CustomerItem form) {
        jdbcClient.sql("""
                INSERT INTO interior_cms.construction_projects
                    (company_id, version, created_at, updated_at, customer_id, name, address,
                     construction_type, occupied, status, on_hold, request_key)
                VALUES (:companyId, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :customerId,
                        :name, :address, '미정', FALSE, 'CONSULTATION', FALSE, :requestKey)
                """)
                .param("companyId", companyId).param("customerId", customerId)
                .param("name", form.getProjectName()).param("address", form.getSiteAddress())
                .param("requestKey", form.getRequestKey()).update();
    }

    public void insertConsultation(long companyId, long customerId, long authorId, int sortOrder,
                                   CustomerItem.ConsultationItem item) {
        jdbcClient.sql("""
                INSERT INTO interior_cms.customer_consultation_items
                    (company_id, version, created_at, updated_at, customer_id, author_id,
                     sort_order, category, custom_category, content)
                VALUES (:companyId, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :customerId, :authorId,
                        :sortOrder, :category, :customCategory, :content)
                """)
                .param("companyId", companyId).param("customerId", customerId)
                .param("authorId", authorId).param("sortOrder", sortOrder)
                .param("category", item.getCategory())
                .param("customCategory", "기타".equals(item.getCategory()) ? item.getCustomCategory() : null)
                .param("content", item.getContent()).update();
    }

    public List<CustomerItem.ConsultationItem> findConsultationItems(long companyId, long customerId) {
        return jdbcClient.sql("""
                SELECT i.category, i.custom_category, i.content
                FROM interior_cms.customer_consultation_items i
                JOIN interior_cms.customers c ON c.company_id = i.company_id AND c.id = i.customer_id
                WHERE c.company_id = :companyId AND c.id = :customerId AND c.archived = FALSE
                ORDER BY i.sort_order, i.id
                """)
                .param("companyId", companyId).param("customerId", customerId)
                .query((rs, rowNum) -> {
                    var item = new CustomerItem.ConsultationItem();
                    item.setCategory(rs.getString("category"));
                    item.setCustomCategory(rs.getString("custom_category"));
                    item.setContent(rs.getString("content"));
                    return item;
                }).list();
    }

    public boolean registrationExists(long companyId, String requestKey) {
        return jdbcClient.sql("""
                SELECT COUNT(*) FROM interior_cms.construction_projects
                WHERE company_id = :companyId AND request_key = :requestKey
                """)
                .param("companyId", companyId).param("requestKey", requestKey)
                .query(Long.class).single() > 0;
    }

    public int updateCustomer(long companyId, CustomerItem current, CustomerItem form, boolean archive) {
        return jdbcClient.sql("""
                UPDATE interior_cms.customers
                SET name = :name, phone = :phone, archived = :archived,
                    version = version + 1, updated_at = CURRENT_TIMESTAMP
                WHERE company_id = :companyId AND id = :customerId AND archived = FALSE AND version = :version
                """)
                .param("companyId", companyId).param("customerId", current.getCustomerId())
                .param("version", form.getCustomerVersion()).param("archived", archive)
                .param("name", form.getCustomerName()).param("phone", form.getCustomerPhone()).update();
    }

    public int updateProject(long companyId, long projectId, CustomerItem form) {
        return jdbcClient.sql("""
                UPDATE interior_cms.construction_projects
                SET name = :name, address = :address, detail_address = NULL,
                    version = version + 1, updated_at = CURRENT_TIMESTAMP
                WHERE company_id = :companyId AND id = :projectId AND version = :version
                """)
                .param("companyId", companyId).param("projectId", projectId)
                .param("version", form.getProjectVersion()).param("name", form.getProjectName())
                .param("address", form.getSiteAddress()).update();
    }

    public void deleteConsultationItems(long companyId, long customerId) {
        jdbcClient.sql("DELETE FROM interior_cms.customer_consultation_items WHERE company_id = :companyId AND customer_id = :customerId")
                .param("companyId", companyId).param("customerId", customerId).update();
    }
}
