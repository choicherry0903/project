package com.roofandwall.interior;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

import com.roofandwall.interior.auth.model.*;
import com.roofandwall.interior.auth.repository.StaffAccountRepository;
import com.roofandwall.interior.auth.service.*;
import com.roofandwall.interior.customer.model.Customer;
import com.roofandwall.interior.customer.repository.CustomerRepository;
import com.roofandwall.interior.project.model.*;
import com.roofandwall.interior.project.repository.ConstructionProjectRepository;
import com.roofandwall.interior.estimate.model.*;
import com.roofandwall.interior.estimate.repository.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EstimateIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired StaffAccountRepository accounts;
    @Autowired CustomerRepository customers;
    @Autowired ConstructionProjectRepository projects;
    @Autowired EstimateRepository estimates;
    @Autowired EstimateItemRepository items;
    @MockitoBean VerificationEmailSender verificationEmailSender;
    StaffPrincipal admin;
    ConstructionProject project;
    @BeforeEach void setup() {
        admin = new StaffPrincipal(accounts.findByUsernameIgnoreCase("test-admin").orElseThrow());
        project = project(1L);
    }
    private ConstructionProject project(Long companyId) {
        var c = customers.saveAndFlush(new Customer(companyId, "견적 고객", "01012345678", null, null,
            UUID.randomUUID().toString(), null, false));
        return projects.saveAndFlush(new ConstructionProject(companyId, c.getId(), "견적 테스트 현장", "서울 테스트 주소",
            null, "도배", null, null, false, null, ProjectStatus.CONSULTATION, false, null, UUID.randomUUID().toString()));
    }
    private MockHttpServletRequestBuilder draft(String path, String key) {
        return post(path).with(user(admin)).with(csrf()).param("projectId", project.getId().toString())
            .param("validUntil", "2026-10-31").param("requestKey", key).param("discount", "100000").param("taxRate", "0.10")
            .param("items[0].category", "MATERIAL").param("items[0].name", "실크 벽지")
            .param("items[0].unit", "롤").param("items[0].quantity", "5").param("items[0].unitPrice", "80000")
            .param("items[1].category", "LABOR").param("items[1].name", "도배 시공")
            .param("items[1].unit", "인일").param("items[1].quantity", "4").param("items[1].unitPrice", "350000")
            .param("items[2].category", "DEMOLITION").param("items[2].name", "철거·바탕 보수")
            .param("items[2].unit", "식").param("items[2].quantity", "1").param("items[2].unitPrice", "200000")
            .param("items[3].category", "TRANSPORT").param("items[3].name", "운반")
            .param("items[3].unit", "식").param("items[3].quantity", "1").param("items[3].unitPrice", "80000");
    }
    @Test void createEditDeleteAndPptCalculation() throws Exception {
        mvc.perform(get("/estimates").with(user(admin))).andExpect(status().isOk()).andExpect(content().string(containsString("견적 관리")));
        mvc.perform(get("/estimates/new").with(user(admin))).andExpect(status().isOk()).andExpect(content().string(containsString("자재 추가")));
        String key = UUID.randomUUID().toString();
        mvc.perform(draft("/estimates", key).param("displayedTotal", "2178000")).andExpect(status().is3xxRedirection());
        var e = estimates.findByCompanyIdAndRequestKey(1L, key).orElseThrow();
        assertThat(e.getTotalAmount()).isEqualTo(2178000);
        assertThat(e.getSupplyAmount()).isEqualTo(1980000);
        assertThat(items.findByCompanyIdAndEstimateIdOrderByIdAsc(1L, e.getId())).hasSize(4);
        mvc.perform(draft("/estimates", key)).andExpect(redirectedUrl("/estimates/" + e.getId()));
        assertThat(estimates.count()).isEqualTo(1);
        mvc.perform(get("/estimates/" + e.getId()).with(user(admin))).andExpect(status().isOk()).andExpect(content().string(containsString("2,178,000원")));
        mvc.perform(get("/estimates/" + e.getId() + "/edit").with(user(admin))).andExpect(status().isOk()).andExpect(content().string(containsString("실크 벽지")));
        mvc.perform(get("/estimates/" + e.getId() + "/edit").with(user(admin))).andExpect(content().string(containsString("value=\"2026-10-31\"")));
        long oldVersion = e.getVersion();
        // Editing only a line name must also advance the estimate version.
        var request = draft("/estimates/" + e.getId(), key).param("version", Long.toString(oldVersion));
        request.param("items[4].name", "추가 메모 항목");
        mvc.perform(request).andExpect(status().is3xxRedirection());
        assertThat(e.getVersion()).isGreaterThan(oldVersion);
        mvc.perform(draft("/estimates/" + e.getId(), key).param("version", Long.toString(oldVersion)))
            .andExpect(status().isOk()).andExpect(model().attributeHasErrors("form"));
        mvc.perform(get("/estimates/" + e.getId() + "/delete").with(user(admin))).andExpect(status().isOk());
        mvc.perform(post("/estimates/" + e.getId() + "/delete").with(user(admin)).with(csrf()).param("version", Long.toString(oldVersion)))
            .andExpect(redirectedUrl("/estimates/" + e.getId()));
        assertThat(e.isArchived()).isFalse();
        mvc.perform(post("/estimates/" + e.getId() + "/delete").with(user(admin)).with(csrf()).param("version", Long.toString(e.getVersion())))
            .andExpect(redirectedUrl("/estimates"));
        assertThat(e.isArchived()).isTrue();
        mvc.perform(get("/estimates/" + e.getId()).with(user(admin))).andExpect(status().isNotFound());
        mvc.perform(draft("/estimates", UUID.randomUUID().toString())).andExpect(status().is3xxRedirection());
        assertThat(estimates.latestRevision(1L, project.getId())).isEqualTo(2);
    }
    @Test void incompleteDraftAndValidation() throws Exception {
        String key = UUID.randomUUID().toString();
        mvc.perform(post("/estimates").with(user(admin)).with(csrf()).param("projectId", project.getId().toString())
            .param("requestKey", key).param("taxRate", "0").param("items[0].name", "미완성 자재"))
            .andExpect(status().is3xxRedirection());
        var e = estimates.findByCompanyIdAndRequestKey(1L, key).orElseThrow();
        assertThat(e.getTotalAmount()).isZero();
        assertThat(items.findByCompanyIdAndEstimateIdOrderByIdAsc(1L, e.getId()).getFirst().isUnitPriceMissing()).isTrue();
        mvc.perform(get("/estimates/" + e.getId() + "/edit").with(user(admin))).andExpect(status().isOk())
            .andExpect(content().string(containsString("<option value=\"0\" selected=\"selected\">0%</option>")));
        mvc.perform(draft("/estimates", UUID.randomUUID().toString()).param("displayedTotal", "1"))
            .andExpect(status().isOk()).andExpect(model().attributeHasErrors("form"));
        mvc.perform(post("/estimates").with(user(admin)).with(csrf()).param("projectId", project.getId().toString())
            .param("items[0].quantity", "-2"))
            .andExpect(status().isOk()).andExpect(model().attributeHasErrors("form"));
        assertThat(estimates.count()).isEqualTo(1);
    }
    @Test void emptyDraftDoesNotCreatePhantomItems() throws Exception {
        String key = UUID.randomUUID().toString();
        mvc.perform(post("/estimates").with(user(admin)).with(csrf())
            .param("projectId", project.getId().toString()).param("requestKey", key))
            .andExpect(status().is3xxRedirection());
        var e = estimates.findByCompanyIdAndRequestKey(1L, key).orElseThrow();
        assertThat(items.findByCompanyIdAndEstimateIdOrderByIdAsc(1L, e.getId())).isEmpty();
        mvc.perform(get("/estimates/" + e.getId()).with(user(admin)))
            .andExpect(content().string(containsString("견적 항목을 추가해 주세요.")));
    }

    @Test void tenantWorkerCsrfAndConfirmedProtection() throws Exception {
        var foreign = project(2L);
        var confirmed = estimates.saveAndFlush(new Estimate(1L, project.getId(), 1, EstimateStatus.CONFIRMED,
            null, null, null, null, 0L, BigDecimal.ZERO, 0L, 0L, 0L, null, null));
        mvc.perform(get("/estimates/new").with(user(admin)).param("projectId", foreign.getId().toString())).andExpect(status().isNotFound());
        mvc.perform(post("/estimates").with(user(admin)).with(csrf()).param("projectId", foreign.getId().toString())).andExpect(status().isNotFound());
        var other = estimates.saveAndFlush(new Estimate(2L, foreign.getId(), 1, EstimateStatus.DRAFT,
            null, null, null, null, 0L, BigDecimal.ZERO, 0L, 0L, 0L, null, null));
        mvc.perform(get("/estimates/" + other.getId()).with(user(admin))).andExpect(status().isNotFound());
        mvc.perform(post("/estimates/" + other.getId() + "/delete").with(user(admin)).with(csrf()).param("version", "0")).andExpect(status().isNotFound());
        mvc.perform(draft("/estimates/" + confirmed.getId(), UUID.randomUUID().toString()).param("version", "0"))
            .andExpect(status().isOk()).andExpect(model().attributeHasErrors("form"));
        mvc.perform(post("/estimates/" + confirmed.getId() + "/delete").with(user(admin)).with(csrf()).param("version", "0"))
            .andExpect(redirectedUrl("/estimates/" + confirmed.getId()));
        assertThat(confirmed.isArchived()).isFalse();
        var worker = accounts.saveAndFlush(new StaffAccount(1L, "estimate-worker", "unused", "작업자", Role.WORKER));
        mvc.perform(get("/estimates").with(user(new StaffPrincipal(worker)))).andExpect(status().isForbidden());
        mvc.perform(post("/estimates").with(user(admin))).andExpect(status().isForbidden());
    }
}
