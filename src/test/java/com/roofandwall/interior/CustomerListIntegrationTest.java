package com.roofandwall.interior;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestExecutionListeners(listeners = {
        DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class
}, inheritListeners = false)
@Transactional
class CustomerListIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void readsJoinedProjectsNewestFirstAndEscapesCustomerInput() throws Exception {
        insertFixture(1, 1, false, "첫 번째 현장", "기존 고객", "서울 마포구", null, "CONSULTATION");
        insertFixture(2, 1, false, "두 번째 현장", "<script>alert(1)</script>", "서울 성동구", "101호", "IN_PROGRESS");

        String html = mvc.perform(get("/projects").with(TestStaff.signedIn()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("projects", hasSize(2)))
                .andExpect(content().string(containsString("2건")))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(html.indexOf("두 번째 현장")).isLessThan(html.indexOf("첫 번째 현장"));
        assertThat(html).contains("010-1234-5678", "서울 성동구 101호", "상담", "시공");
        assertThat(html).contains("&lt;script&gt;alert(1)&lt;/script&gt;").doesNotContain("<script>alert(1)</script>");
        assertThat(html).doesNotContain("등록된 현장이 없습니다");
    }

    @Test
    void excludesOtherCompaniesAndArchivedCustomers() throws Exception {
        insertFixture(1, 2, false, "다른 업체 현장", "다른 업체 고객", "서울", null, "CONSULTATION");
        insertFixture(2, 1, true, "보관 현장", "보관 고객", "서울", null, "CONSULTATION");
        mvc.perform(get("/projects").with(TestStaff.signedIn()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("projects", hasSize(0)));
    }

    @Test
    void emptyDatabaseShowsEmptyState() throws Exception {
        mvc.perform(get("/projects").with(TestStaff.signedIn()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("projects", hasSize(0)))
                .andExpect(content().string(containsString("등록된 현장이 없습니다")));
    }

    @Test
    void registrationScreenHasEditableFieldsAndDisabledSubmit() throws Exception {
        String html = mvc.perform(get("/projects/regform").with(TestStaff.signedIn()))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/customer-registration"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("name=\"customerName\"", "name=\"customerPhone\"",
                "name=\"projectName\"", "name=\"siteAddress\"", "type=\"submit\" disabled");
        assertThat(html).contains("입력한 내용은 저장되지 않으며");
        assertThat(projectCount()).isZero();
    }

    @Test
    void registrationPostHasNoHandlerAndDoesNotSave() throws Exception {
        mvc.perform(post("/projects").with(TestStaff.signedIn()).with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .param("customerName", "고객")
                        .param("customerPhone", "010-1234-5678")
                        .param("projectName", "현장")
                        .param("siteAddress", "서울"))
                .andExpect(status().isMethodNotAllowed());
        assertThat(projectCount()).isZero();
    }

    private long projectCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM interior_cms.construction_projects", Long.class);
    }

    private void insertFixture(long id, long companyId, boolean archived, String projectName,
                               String customerName, String address, String detailAddress, String status) {
        jdbc.update("""
                INSERT INTO interior_cms.customers (id, company_id, name, phone, archived)
                VALUES (?, ?, ?, ?, ?)
                """, id, companyId, customerName, "010-1234-5678", archived);
        jdbc.update("""
                INSERT INTO interior_cms.construction_projects
                (id, company_id, customer_id, name, address, detail_address, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, id, companyId, id, projectName, address, detailAddress, status);
    }
}
