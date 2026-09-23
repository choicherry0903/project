package com.barunsigong.interior;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.barunsigong.interior.auth.model.*;
import com.barunsigong.interior.auth.repository.StaffAccountRepository;
import com.barunsigong.interior.auth.service.StaffPrincipal;
import com.barunsigong.interior.customer.model.Customer;
import com.barunsigong.interior.customer.repository.CustomerRepository;
import com.barunsigong.interior.project.repository.ConstructionProjectRepository;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class InteriorCmsIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    StaffAccountRepository accounts;

    @Autowired
    CustomerRepository customers;

    @Autowired
    ConstructionProjectRepository projects;

    StaffPrincipal admin;

    @BeforeEach
    void principal() {
        admin = new StaffPrincipal(accounts.findByEmailIgnoreCase("test-admin@example.test").orElseThrow());
    }

    @Test
    void loginAndCsrfAreRequired() throws Exception {
        mvc.perform(get("/customers")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("이메일")));
        mvc.perform(post("/customers").with(user(admin))).andExpect(status().isForbidden());
    }

    @Test
    void databasePasswordAuthenticationWorks() throws Exception {
        mvc.perform(
            post("/login")
                .with(csrf())
                .param("email", "test-admin@example.test")
                .param("password", "TestOnlyPassword!2026")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));
        mvc.perform(
            post("/login").with(csrf()).param("email", "test-admin@example.test").param("password", "wrong")
        ).andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void customerAndProjectFlowPersistsAndRenders() throws Exception {
        String key = UUID.randomUUID().toString();
        mvc.perform(
            post("/customers")
                .with(user(admin))
                .with(csrf())
                .param("name", "테스트고객")
                .param("phone", "010-1234-5678")
                .param("memo", "초기 상담")
                .param("requestKey", key)
        ).andExpect(status().is3xxRedirection());
        var customer = customers.findByRequestKeyAndCompanyId(key, 1L).orElseThrow();
        assertThat(customer.getPhone()).isEqualTo("01012345678");
        mvc.perform(get("/customers/" + customer.getId()).with(user(admin)))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("테스트고객")));
        mvc.perform(get("/customers/" + customer.getId() + "/edit").with(user(admin))).andExpect(status().isOk());
        String projectKey = UUID.randomUUID().toString();
        mvc.perform(
            post("/projects")
                .with(user(admin))
                .with(csrf())
                .param("customerId", customer.getId().toString())
                .param("name", "성수 테스트 현장")
                .param("address", "서울 테스트 주소")
                .param("constructionType", "도배")
                .param("requestKey", projectKey)
        ).andExpect(status().is3xxRedirection());
        var project = projects.findByRequestKeyAndCompanyId(projectKey, 1L).orElseThrow();
        mvc.perform(get("/projects/" + project.getId()).with(user(admin))).andExpect(status().isOk());
        mvc.perform(get("/projects/" + project.getId() + "/edit").with(user(admin))).andExpect(status().isOk());
        mvc.perform(get("/projects/" + project.getId() + "/survey").with(user(admin))).andExpect(status().isOk());
        mvc.perform(get("/customers").with(user(admin)))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("010-****-5678")));
        mvc.perform(get("/projects").with(user(admin))).andExpect(status().isOk());
        mvc.perform(get("/dashboard").with(user(admin))).andExpect(status().isOk());
    }

    @Test
    void validationRetainsInputAndPreventsSave() throws Exception {
        mvc.perform(
            post("/customers")
                .with(user(admin))
                .with(csrf())
                .param("name", "김")
                .param("phone", "abc")
                .param("requestKey", UUID.randomUUID().toString())
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("form", "name", "phone"));
        assertThat(customers.countByCompanyId(1L)).isZero();
    }

    @Test
    void repeatCreationWithSameKeyIsIdempotent() throws Exception {
        String key = UUID.randomUUID().toString();
        for (int i = 0; i < 2; i++) mvc.perform(
            post("/customers")
                .with(user(admin))
                .with(csrf())
                .param("name", "반복고객")
                .param("phone", "01012345678")
                .param("requestKey", key)
        ).andExpect(status().is3xxRedirection());
        assertThat(customers.countByCompanyId(1L)).isEqualTo(1);
    }

    @Test
    void staleUpdateIsRejected() throws Exception {
        var customer = customers.saveAndFlush(
            new Customer(
                1L,
                "원본고객",
                "01012345678",
                null,
                null,
                UUID.randomUUID().toString(),
                admin.getAccountId(),
                false
            )
        );
        long oldVersion = customer.getVersion();
        customer.updateDetails("변경고객", customer.getPhone(), null, null);
        customers.flush();
        mvc.perform(
            post("/customers/" + customer.getId())
                .with(user(admin))
                .with(csrf())
                .param("name", "오래된수정")
                .param("phone", customer.getPhone())
                .param("requestKey", customer.getRequestKey())
                .param("version", Long.toString(oldVersion))
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasErrors("form"));
        assertThat(customer.getName()).isEqualTo("변경고객");
    }

    @Test
    void crossCompanyCustomerIsNotAccessible() throws Exception {
        var other = customers.saveAndFlush(
            new Customer(2L, "다른업체고객", "01000009999", null, null, UUID.randomUUID().toString(), null, false)
        );
        mvc.perform(get("/customers/" + other.getId()).with(user(admin))).andExpect(status().isNotFound());
        mvc.perform(
            post("/projects")
                .with(user(admin))
                .with(csrf())
                .param("customerId", other.getId().toString())
                .param("name", "잘못된현장")
                .param("address", "주소")
                .param("constructionType", "도배")
                .param("requestKey", UUID.randomUUID().toString())
        ).andExpect(status().isNotFound());
    }

    @Test
    void workerCannotReadFinancialOrCustomerScreens() throws Exception {
        var worker = accounts.saveAndFlush(
            new StaffAccount(1L, "worker@example.test", "unused", "작업자", Role.WORKER)
        );
        for (String path : new String[] {
            "/customers",
            "/projects",
            "/estimates",
            "/contracts",
            "/payments",
            "/costs",
            "/settings",
        })
            mvc.perform(get(path).with(user(new StaffPrincipal(worker)))).andExpect(status().isForbidden());
        mvc.perform(get("/mobile/tasks").with(user(new StaffPrincipal(worker)))).andExpect(status().isOk());
        mvc.perform(get("/").with(user(new StaffPrincipal(worker)))).andExpect(redirectedUrl("/mobile/tasks"));
    }

    @Test
    void officeCannotAccessAdministratorSettings() throws Exception {
        var office = accounts.saveAndFlush(
            new StaffAccount(1L, "office@example.test", "unused", "사무담당", Role.OFFICE)
        );
        mvc.perform(get("/settings").with(user(new StaffPrincipal(office)))).andExpect(status().isForbidden());
    }

    @Test
    void allScaffoldScreensRender() throws Exception {
        for (String path : new String[] {
            "/dashboard",
            "/customers/new",
            "/projects/new",
            "/estimates",
            "/estimates/preview",
            "/estimates/history",
            "/contracts",
            "/schedules",
            "/schedules/new",
            "/work-logs",
            "/materials",
            "/purchase-orders",
            "/costs",
            "/payments",
            "/completion",
            "/after-service",
            "/mobile/tasks",
            "/mobile/task-detail",
            "/notifications",
            "/settings",
        })
            mvc.perform(get(path).with(user(admin))).andExpect(status().isOk());
    }
}
