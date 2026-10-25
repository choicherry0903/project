package com.roofandwall.interior;

import com.roofandwall.interior.customer.dto.CustomerItem;
import com.roofandwall.interior.customer.service.CustomerQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestExecutionListeners(listeners = DependencyInjectionTestExecutionListener.class, inheritListeners = false)
class CustomerModificationIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired CustomerQueryService service;

    @BeforeEach
    void fixtures() {
        cleanup();
        jdbc.update("INSERT INTO interior_cms.staff_accounts (id, company_id, username, password_hash, display_name, role, active) VALUES (1, 1, 'edit-staff', 'unused', '직원', 'OFFICE', TRUE)");
        for (int company = 1; company <= 2; company++) {
            jdbc.update("INSERT INTO interior_cms.customers (id, company_id, name, phone, archived) VALUES (?, ?, '원래 고객', '010-1234-5678', FALSE)", company, company);
            jdbc.update("INSERT INTO interior_cms.construction_projects (id, company_id, customer_id, name, address, detail_address, status) VALUES (?, ?, ?, '원래 현장', '서울', '101호', 'IN_PROGRESS')", company, company, company);
        }
        jdbc.update("INSERT INTO interior_cms.construction_projects (id, company_id, customer_id, name, address, status) VALUES (3, 1, 1, '다른 현장', '부산', 'CONSULTATION')");
        jdbc.update("INSERT INTO interior_cms.customer_consultation_items (company_id, customer_id, author_id, sort_order, category, content, created_at, updated_at) VALUES (1, 1, 1, 0, '샷시', '기존 상담', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
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
    void failedConsultationInsertRollsBackAllChanges() {
        var form = service.requireDetail(1, 1);
        form.setCustomerName("저장되면 안 되는 고객");
        assertThatThrownBy(() -> service.updateCustomer(1, 1, 999, form))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        var current = service.requireDetail(1, 1);
        assertThat(current.getCustomerName()).isEqualTo("원래 고객");
        assertThat(current.getCustomerVersion()).isZero();
        assertThat(current.getProjectVersion()).isZero();
        assertThat(current.getConsultationItems()).singleElement()
                .extracting(CustomerItem.ConsultationItem::getContent).isEqualTo("기존 상담");
    }

    @Test
    void rendersPrefilledEditAndConfirmationWithCsrf() throws Exception {
        mvc.perform(get("/customers/1/edit").with(TestStaff.signedIn()))
                .andExpect(status().isOk()).andExpect(content().string(containsString("수정 저장")))
                .andExpect(content().string(containsString("서울 101호")))
                .andExpect(content().string(containsString("기존 상담")))
                .andExpect(content().string(containsString("_csrf")));
        mvc.perform(get("/customers/1/delete").with(TestStaff.signedIn()))
                .andExpect(status().isOk()).andExpect(content().string(containsString("모든 현장")))
                .andExpect(content().string(containsString("_csrf")));
    }

    @Test
    void editsFieldsAndReplacesConsultationsWithoutChangingStatusOrOtherProject() throws Exception {
        mvc.perform(edit().param("companyId", "2").param("customerId", "2").param("status", "COMPLETED")
                        .param("consultationItems[0].category", "기타").param("consultationItems[0].customCategory", "방음")
                        .param("consultationItems[0].content", "새 상담"))
                .andExpect(redirectedUrl("/customers/detail?projectId=1"));
        var result = service.requireDetail(1, 1);
        assertThat(result.getCustomerName()).isEqualTo("수정 고객");
        assertThat(result.getSiteAddress()).isEqualTo("수원 202호");
        assertThat(result.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(result.getConsultationItems()).singleElement().extracting(CustomerItem.ConsultationItem::getContent).isEqualTo("새 상담");
        assertThat(service.requireDetail(1, 3).getProjectName()).isEqualTo("다른 현장");
        assertThat(service.requireDetail(2, 2).getCustomerName()).isEqualTo("원래 고객");
        assertThat(jdbc.queryForObject("SELECT detail_address FROM interior_cms.construction_projects WHERE id=1", String.class)).isNull();
    }

    @Test
    void removingAllConsultationsIsSaved() throws Exception {
        mvc.perform(edit()).andExpect(status().is3xxRedirection());
        assertThat(service.requireDetail(1, 1).getConsultationItems()).isEmpty();
    }

    @Test
    void validationPreservesInputWithoutWriting() throws Exception {
        mvc.perform(edit().param("consultationItems[0].category", "기타")
                        .param("consultationItems[0].content", "유지할 내용"))
                .andExpect(status().isOk()).andExpect(model().hasErrors())
                .andExpect(content().string(containsString("유지할 내용")));
        assertThat(service.requireDetail(1, 1).getCustomerName()).isEqualTo("원래 고객");
    }

    @Test
    void staleProjectRollsBackCustomerUpdateAndPreservesConsultations() throws Exception {
        jdbc.update("UPDATE interior_cms.construction_projects SET version=1 WHERE id=1");
        mvc.perform(edit()).andExpect(model().hasErrors())
                .andExpect(content().string(containsString("최신 정보를 다시")));
        assertThat(service.requireDetail(1, 1).getCustomerName()).isEqualTo("원래 고객");
        assertThat(service.requireDetail(1, 1).getCustomerVersion()).isZero();
        assertThat(service.requireDetail(1, 1).getConsultationItems()).hasSize(1);
    }

    @Test
    void duplicateEditAndStaleDeleteAreRejected() throws Exception {
        mvc.perform(edit()).andExpect(status().is3xxRedirection());
        mvc.perform(edit()).andExpect(model().hasErrors());
        mvc.perform(deletion().param("confirmed", "true")).andExpect(model().hasErrors());
        assertThat(service.findDetail(1, 1)).isPresent();
    }

    @Test
    void deletionRequiresConfirmationAndArchivesEveryLinkedProject() throws Exception {
        mvc.perform(deletion()).andExpect(model().hasErrors());
        assertThat(service.findAll(1)).hasSize(2);
        mvc.perform(deletion().param("confirmed", "true")).andExpect(redirectedUrl("/customers"));
        assertThat(service.findAll(1)).isEmpty();
        assertThat(service.findDetail(1, 1)).isEmpty();
        assertThat(service.findDetail(1, 3)).isEmpty();
        assertThat(service.findAll(2)).hasSize(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM interior_cms.customer_consultation_items", Long.class)).isEqualTo(1);
        mvc.perform(deletion().param("confirmed", "true")).andExpect(status().isNotFound());
        mvc.perform(edit()).andExpect(status().isNotFound());
    }

    @Test
    void rejectsOtherCompanyMissingProjectsAndCsrf() throws Exception {
        for (String id : new String[]{"2", "999"}) {
            mvc.perform(get("/customers/" + id + "/edit").with(TestStaff.signedIn())).andExpect(status().isNotFound());
            mvc.perform(get("/customers/" + id + "/delete").with(TestStaff.signedIn())).andExpect(status().isNotFound());
            mvc.perform(post("/customers/" + id + "/edit").with(TestStaff.signedIn()).with(csrf())).andExpect(status().isNotFound());
            mvc.perform(post("/customers/" + id + "/delete").with(TestStaff.signedIn()).with(csrf())).andExpect(status().isNotFound());
        }
        for (String action : new String[]{"edit", "delete"})
            mvc.perform(post("/customers/1/" + action).with(TestStaff.signedIn())).andExpect(status().isForbidden());
    }

    @Test
    void missingVersionsCannotMutateData() throws Exception {
        mvc.perform(post("/customers/1/delete").with(TestStaff.signedIn()).with(csrf()).param("confirmed", "true"))
                .andExpect(model().hasErrors());
        assertThat(service.findDetail(1, 1)).isPresent();
    }

    private MockHttpServletRequestBuilder edit() {
        return post("/customers/1/edit").with(TestStaff.signedIn()).with(csrf())
                .param("customerVersion", "0").param("projectVersion", "0")
                .param("customerName", "수정 고객").param("customerPhone", "010-9999-8888")
                .param("projectName", "수정 현장").param("siteAddress", "수원 202호");
    }

    private MockHttpServletRequestBuilder deletion() {
        return post("/customers/1/delete").with(TestStaff.signedIn()).with(csrf())
                .param("customerVersion", "0").param("projectVersion", "0");
    }
}
