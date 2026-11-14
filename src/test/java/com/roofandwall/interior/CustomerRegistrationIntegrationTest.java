package com.roofandwall.interior;

import com.roofandwall.interior.customer.dto.CustomerItem;
import com.roofandwall.interior.customer.service.CustomerQueryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestExecutionListeners(listeners = DependencyInjectionTestExecutionListener.class, inheritListeners = false)
class CustomerRegistrationIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired CustomerQueryService service;

    @BeforeEach
    void setup() {
        cleanup();
        jdbc.update("""
                INSERT INTO interior_cms.staff_accounts
                (id, company_id, username, password_hash, display_name, role, active)
                VALUES (1, 1, 'registration-staff', 'unused', '직원', 'OFFICE', TRUE)
                """);
    }

    @AfterEach
    void cleanup() {
        jdbc.update("DELETE FROM interior_cms.customer_consultation_items");
        jdbc.update("DELETE FROM interior_cms.consultations");
        jdbc.update("DELETE FROM interior_cms.construction_projects");
        jdbc.update("DELETE FROM interior_cms.customers");
        jdbc.update("DELETE FROM interior_cms.staff_accounts");
    }

    @Test
    void savesLinkedCustomerProjectAndStructuredConsultations() throws Exception {
        mvc.perform(request(UUID.randomUUID().toString())
                        .param("companyId", "999").param("status", "COMPLETED")
                        .param("consultationItems[0].category", "샷시")
                        .param("consultationItems[0].content", "창호 교체")
                        .param("consultationItems[1].category", "기타")
                        .param("consultationItems[1].customCategory", "방음")
                        .param("consultationItems[1].content", "방음 공사"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/customers"));
        var project = jdbc.queryForMap("SELECT * FROM interior_cms.construction_projects");
        assertThat(project.get("company_id")).isEqualTo(1L);
        assertThat(project.get("status")).isEqualTo("CONSULTATION");
        assertThat(project.get("name")).isEqualTo("새 현장");
        long customerId = ((Number) project.get("customer_id")).longValue();
        var items = service.findConsultationItems(1, customerId);
        assertThat(items).hasSize(2);
        assertThat(items.get(0).getCategory()).isEqualTo("샷시");
        assertThat(items.get(0).getContent()).isEqualTo("창호 교체");
        assertThat(items.get(1).getCustomCategory()).isEqualTo("방음");
        assertThat(service.findConsultationItems(2, customerId)).isEmpty();
        mvc.perform(get("/customers").with(TestStaff.signedIn()))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("새 현장")));
    }

    @Test
    void duplicateRequestDoesNotInsertAgain() throws Exception {
        String key = UUID.randomUUID().toString();
        for (int i = 0; i < 2; i++) {
            mvc.perform(request(key).param("consultationItems[0].category", "샷시")
                            .param("consultationItems[0].content", "상담"))
                    .andExpect(redirectedUrl("/customers"));
        }
        assertThat(count("customers")).isEqualTo(1);
        assertThat(count("construction_projects")).isEqualTo(1);
        assertThat(count("customer_consultation_items")).isEqualTo(1);
    }

    @Test
    void simultaneousRequestsAreAlsoDeduplicated() throws Exception {
        String key = UUID.randomUUID().toString();
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> { start.await(); return mvc.perform(request(key)).andReturn().getResponse(); });
            var second = executor.submit(() -> { start.await(); return mvc.perform(request(key)).andReturn().getResponse(); });
            start.countDown();
            assertThat(first.get(10, TimeUnit.SECONDS).getRedirectedUrl()).isEqualTo("/customers");
            assertThat(second.get(10, TimeUnit.SECONDS).getRedirectedUrl()).isEqualTo("/customers");
        }
        assertThat(count("customers")).isEqualTo(1);
        assertThat(count("construction_projects")).isEqualTo(1);
    }

    @Test
    void invalidInputReturnsAllEnteredRowsWithoutSaving() throws Exception {
        String key = UUID.randomUUID().toString();
        String html = mvc.perform(request(key).with(req -> { req.setParameter("customerName", " "); return req; })
                        .param("consultationItems[0].category", "기타")
                        .param("consultationItems[0].customCategory", "맞춤 상담")
                        .param("consultationItems[0].content", "유지할 내용"))
                .andExpect(status().isOk()).andExpect(view().name("customer/customer-registration"))
                .andExpect(model().attributeHasFieldErrors("form", "customerName"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("유지할 내용", "맞춤 상담", key, "role=\"alert\"");
        assertThat(html).contains("value=\"기타\" selected=\"selected\"");
        java.nio.file.Files.writeString(java.nio.file.Path.of("target/registration-error-preview.html"), html);
        assertThat(count("customers")).isZero();
    }

    @Test
    void rejectsExcessLengthAndMissingCustomCategory() throws Exception {
        mvc.perform(request(UUID.randomUUID().toString()).with(req -> { req.setParameter("customerName", "가".repeat(51)); return req; })
                        .param("consultationItems[0].category", "기타")
                        .param("consultationItems[0].content", "상담"))
                .andExpect(model().attributeHasFieldErrors("form", "customerName", "consultationItems[0].customCategory"));
        assertThat(count("customers")).isZero();
    }

    @Test
    void missingRequestKeyIsRejectedAndCanBeRetried() throws Exception {
        mvc.perform(request("invalid"))
                .andExpect(model().attributeHasFieldErrors("form", "requestKey"))
                .andExpect(view().name("customer/customer-registration"));
        assertThat(count("customers")).isZero();
    }

    @Test
    void consultationFailureRollsBackCustomerAndProject() {
        var form = new CustomerItem();
        form.setRequestKey(UUID.randomUUID().toString());
        form.setCustomerName("고객"); form.setCustomerPhone("010-1234-5678");
        form.setProjectName("현장"); form.setSiteAddress("서울");
        var item = new CustomerItem.ConsultationItem();
        item.setCategory("샷시"); item.setContent("상담");
        form.getConsultationItems().add(item);
        assertThatThrownBy(() -> service.insertCustomer(1, 999, form)).isInstanceOf(DataIntegrityViolationException.class);
        assertThat(count("customers")).isZero();
        assertThat(count("construction_projects")).isZero();
    }

    @Test
    void databaseFailurePreservesInputAndAllowsRetry() throws Exception {
        jdbc.update("DELETE FROM interior_cms.staff_accounts");
        mvc.perform(request(UUID.randomUUID().toString())
                        .param("consultationItems[0].category", "샷시")
                        .param("consultationItems[0].content", "복원할 상담"))
                .andExpect(status().isOk()).andExpect(model().hasErrors())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("복원할 상담")));
        assertThat(count("customers")).isZero();
    }

    private MockHttpServletRequestBuilder request(String key) {
        return post("/customers/insertCustomer").with(TestStaff.signedIn()).with(csrf())
                .param("requestKey", key).param("customerName", "고객")
                .param("customerPhone", "010-1234-5678").param("projectName", "새 현장").param("siteAddress", "서울");
    }

    private long count(String table) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM interior_cms." + table, Long.class);
    }
}
