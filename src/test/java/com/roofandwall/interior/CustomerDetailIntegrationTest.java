package com.roofandwall.interior;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestExecutionListeners(listeners = {DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class}, inheritListeners = false)
@Transactional
class CustomerDetailIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void fixtures() {
        fixture(11, 101, 1, false);
        fixture(22, 202, 2, false);
        fixture(33, 303, 1, true);
        jdbc.update("""
                INSERT INTO interior_cms.staff_accounts
                (id, company_id, username, password_hash, display_name, role, active)
                VALUES (1, 1, 'detail-staff', 'unused', '직원', 'OFFICE', TRUE)
                """);
        jdbc.update("""
                INSERT INTO interior_cms.customer_consultation_items
                (company_id, customer_id, author_id, created_at, updated_at, sort_order, category, custom_category, content)
                VALUES (1, 11, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, '기타', '맞춤 방음', '방음 공사'),
                       (1, 11, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, '샷시', NULL, '<script>상담</script>')
                """);
    }

    @Test
    void rendersRegisteredFieldsAndOrderedConsultationsReadOnly() throws Exception {
        String html = mvc.perform(get("/customers/detail").param("projectId", "101").with(TestStaff.signedIn()))
                .andExpect(status().isOk()).andExpect(view().name("customer/customer-detail"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("value=\"테스트 고객\"", "value=\"현장 101\"", "value=\"서울 101호\"",
                "value=\"상담\"", "맞춤 방음", "&lt;script&gt;상담&lt;/script&gt;", "readonly");
        assertThat(html.indexOf("value=\"샷시\"")).isLessThan(html.indexOf("value=\"기타\""));
        assertThat(html).doesNotContain("<script>상담</script>", "consultation-remove", "add-consultation-row");
        Files.writeString(Path.of("target/detail-preview.html"), html);
    }

    @Test
    void otherCompanyArchivedAndMissingProjectsReturn404() throws Exception {
        for (String id : new String[]{"202", "303", "999"}) {
            mvc.perform(get("/customers/detail").param("projectId", id).with(TestStaff.signedIn()))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void detailHasNoModificationEndpoint() throws Exception {
        mvc.perform(post("/customers/detail").param("projectId", "101")
                        .param("customerName", "수정 요청").with(TestStaff.signedIn()).with(csrf()))
                .andExpect(status().isMethodNotAllowed());
        assertThat(jdbc.queryForObject("SELECT name FROM interior_cms.customers WHERE id = 11", String.class))
                .isEqualTo("테스트 고객");
    }

    private void fixture(long customerId, long projectId, long companyId, boolean archived) {
        jdbc.update("INSERT INTO interior_cms.customers (id, company_id, name, phone, archived) VALUES (?, ?, ?, ?, ?)",
                customerId, companyId, "테스트 고객", "010-1234-5678", archived);
        jdbc.update("""
                INSERT INTO interior_cms.construction_projects
                (id, company_id, customer_id, name, address, detail_address, status)
                VALUES (?, ?, ?, ?, '서울', '101호', 'CONSULTATION')
                """, projectId, companyId, customerId, "현장 " + projectId);
    }
}
