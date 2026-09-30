package com.barunsigong.interior;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.barunsigong.interior.auth.model.*;
import com.barunsigong.interior.auth.repository.StaffAccountRepository;
import com.barunsigong.interior.auth.repository.UsernameLookupAttemptRepository;
import com.barunsigong.interior.auth.service.StaffPrincipal;
import com.barunsigong.interior.auth.service.VerificationEmailSender;
import com.barunsigong.interior.customer.model.Customer;
import com.barunsigong.interior.customer.repository.CustomerRepository;
import com.barunsigong.interior.project.repository.ConstructionProjectRepository;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
    UsernameLookupAttemptRepository lookupAttempts;

    @Autowired
    CustomerRepository customers;

    @Autowired
    ConstructionProjectRepository projects;

    @Autowired
    PasswordEncoder encoder;

    @MockitoBean
    VerificationEmailSender verificationEmailSender;

    StaffPrincipal admin;

    @Autowired
    com.barunsigong.interior.material.repository.MaterialRepository materials;

    @Test
    void materialCrudAndClassificationValidation() throws Exception {
        mvc.perform(get("/materials").with(user(admin)))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("/materials/new")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("2뎁스 전체")));
        mvc.perform(get("/materials/new").with(user(admin)))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("600X600")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("싱크볼")))
            .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("<optgroup"))));
        mvc.perform(
            post("/materials")
                .with(user(admin))
                .with(csrf())
                .param("category", "ENTRANCE")
                .param("subcategory", "FLOOR_TILE")
                .param("name", "피에트라 그레이")
                .param("brand", "예림")
                .param("size", "600X600")
                .param("color", "그레이")
        ).andExpect(redirectedUrlPattern("/materials/*"));
        var material = materials
            .findAll()
            .stream()
            .filter(m -> "피에트라 그레이".equals(m.getName()))
            .findFirst()
            .orElseThrow();
        assertThat(material.getSize()).isEqualTo("600X600");
        mvc.perform(get("/materials").with(user(admin)))
            .andExpect(status().isOk())
            .andExpect(
                content().string(org.hamcrest.Matchers.containsString("href=\"/materials/" + material.getId() + "\""))
            )
            .andExpect(
                content().string(
                    org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("data-material-delete"))
                )
            )
            .andExpect(
                content().string(
                    org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("/materials/" + material.getId() + "/edit")
                    )
                )
            );
        mvc.perform(get("/materials/" + material.getId()).with(user(admin)))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("data-material-delete")))
            .andExpect(
                content().string(org.hamcrest.Matchers.containsString("/materials/" + material.getId() + "/edit"))
            );
        mvc.perform(get("/materials/" + material.getId() + "/edit").with(user(admin))).andExpect(status().isOk());
        mvc.perform(
            post("/materials/" + material.getId())
                .with(user(admin))
                .with(csrf())
                .param("version", String.valueOf(material.getVersion()))
                .param("category", "ENTRANCE")
                .param("subcategory", "SINK")
                .param("name", "콰이어트860")
                .param("brand", "백조")
        ).andExpect(redirectedUrl("/materials/" + material.getId()));
        assertThat(materials.findById(material.getId()).orElseThrow().getName()).isEqualTo("콰이어트860");
        assertThat(material.getSubcategoryLabel()).isEqualTo("싱크볼");
        mvc.perform(get("/materials").with(user(admin)).param("subcategory", "SINK"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("콰이어트860")));
        mvc.perform(get("/materials").with(user(admin)).param("subcategory", "FLOOR_TILE"))
            .andExpect(status().isOk())
            .andExpect(
                content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("콰이어트860")))
            );
        mvc.perform(
            post("/materials")
                .with(user(admin))
                .with(csrf())
                .param("category", "ENTRANCE")
                .param("subcategory", "UNKNOWN")
                .param("name", "잘못된 분류")
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasErrors("form"));
        mvc.perform(post("/materials/" + material.getId() + "/delete").with(user(admin))).andExpect(
            status().isForbidden()
        );
        mvc.perform(
            post("/materials/" + material.getId() + "/delete")
                .with(user(admin))
                .with(csrf())
                .param("version", String.valueOf(material.getVersion()))
        ).andExpect(redirectedUrl("/materials"));
        assertThat(materials.findById(material.getId())).isEmpty();
    }

    @Test
    void materialAccessIsScopedToCompany() throws Exception {
        var foreign = materials.saveAndFlush(
            new com.barunsigong.interior.material.model.Material(
                9999L,
                "ENTRANCE",
                null,
                "다른 회사 자재",
                null,
                null,
                "개",
                0L,
                null,
                null,
                true
            )
        );
        mvc.perform(get("/materials/" + foreign.getId() + "/edit").with(user(admin))).andExpect(status().isNotFound());
        mvc.perform(get("/materials/" + foreign.getId()).with(user(admin))).andExpect(status().isNotFound());
        mvc.perform(
            post("/materials/" + foreign.getId() + "/delete")
                .with(user(admin))
                .with(csrf())
                .param("version", "0")
        ).andExpect(status().isNotFound());
        mvc.perform(get("/materials").with(user(admin)))
            .andExpect(status().isOk())
            .andExpect(
                content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("다른 회사 자재")))
            );
    }

    @BeforeEach
    void principal() {
        admin = new StaffPrincipal(accounts.findByUsernameIgnoreCase("test-admin").orElseThrow());
    }

    @Test
    void loginAndCsrfAreRequired() throws Exception {
        mvc.perform(get("/customers")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("아이디")));
        mvc.perform(post("/customers").with(user(admin))).andExpect(status().isForbidden());
    }

    @Test
    void databasePasswordAuthenticationWorks() throws Exception {
        assertThat(accounts.findByUsernameIgnoreCase("test-admin").orElseThrow().getPasswordHash())
            .startsWith("$2a$")
            .isNotEqualTo("TestOnlyPassword!2026");
        mvc.perform(
            post("/login").with(csrf()).param("username", "test-admin").param("password", "TestOnlyPassword!2026")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));
        mvc.perform(post("/login").with(csrf()).param("username", "test-admin").param("password", "wrong")).andExpect(
            redirectedUrl("/login?error")
        );
        mvc.perform(
            post("/login").with(csrf()).param("email", "test-admin").param("password", "TestOnlyPassword!2026")
        ).andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void accountRegistrationAndLookupWorkWithoutPasswordReset() throws Exception {
        mvc.perform(get("/auth/check-username").param("username", "newstaff"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.available").value(true));
        mvc.perform(
            post("/auth/register")
                .with(csrf())
                .param("name", "홍길동")
                .param("phone", "010-1234-5678")
                .param("email", "staff@example.test")
                .param("username", "newstaff")
                .param("password", "initial-pass")
        ).andExpect(status().isOk());
        var account = accounts.findByUsernameIgnoreCase("newstaff").orElseThrow();
        assertThat(account.getPhone()).isEqualTo("01012345678");
        assertThat(account.getEmail()).isEqualTo("staff@example.test");
        assertThat(account.getEmailVerifiedAt()).isNull();
        assertThat(account.getRole()).isEqualTo(Role.OFFICE);
        assertThat(encoder.matches("initial-pass", account.getPasswordHash())).isTrue();
        mvc.perform(
            post("/login").with(csrf()).param("username", "newstaff").param("password", "initial-pass")
        ).andExpect(redirectedUrl("/login?error"));
        ArgumentCaptor<String> link = ArgumentCaptor.forClass(String.class);
        verify(verificationEmailSender).send(org.mockito.ArgumentMatchers.eq("staff@example.test"), link.capture());
        String token = link.getValue().substring(link.getValue().indexOf("token=") + 6);
        mvc.perform(get("/auth/verify-email").param("token", token))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("이메일 확인")));
        mvc.perform(post("/auth/verify-email").with(csrf()).param("token", token))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("이메일 확인이 완료")));
        assertThat(account.getEmailVerifiedAt()).isNotNull();
        mvc.perform(post("/auth/verify-email").with(csrf()).param("token", token)).andExpect(
            content().string(org.hamcrest.Matchers.containsString("이미 사용"))
        );
        mvc.perform(
            post("/login").with(csrf()).param("username", "newstaff").param("password", "initial-pass")
        ).andExpect(redirectedUrl("/"));
        mvc.perform(get("/auth/check-username").param("username", "NEWSTAFF")).andExpect(
            jsonPath("$.available").value(false)
        );
        mvc.perform(
            post("/auth/register")
                .with(csrf())
                .param("name", "다른직원")
                .param("phone", "01098765432")
                .param("email", "other@example.test")
                .param("username", "NEWSTAFF")
                .param("password", "password")
        ).andExpect(status().isBadRequest());
        mvc.perform(post("/auth/find-username").with(csrf()).param("name", "홍길동").param("phone", "01012345678"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("아이디는 ne******입니다."));
        mvc.perform(
            post("/auth/find-username").with(csrf()).param("name", "홍길동").param("phone", "01000000000")
        ).andExpect(status().isBadRequest());
        mvc.perform(
            post("/auth/find-username").with(csrf()).param("name", "홍길동").param("phone", "01000000000")
        ).andExpect(status().isBadRequest());
        mvc.perform(post("/auth/find-username").with(csrf()).param("name", "홍길동").param("phone", "01000000000"))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.message").value("조회에 3회 이상 실패했습니다. 관리자에게 문의해 주세요."));
        mvc.perform(
            post("/auth/find-username").with(csrf()).param("name", "홍길동").param("phone", "01012345678")
        ).andExpect(status().isOk());
        mvc.perform(
            post("/auth/find-username")
                .with(csrf())
                .with(request -> {
                    request.setRemoteAddr("192.0.2.10");
                    return request;
                })
                .param("name", "홍길동")
                .param("phone", "010-0000-0000")
        ).andExpect(status().isTooManyRequests());
        assertThat(
            lookupAttempts
                .findAll()
                .stream()
                .filter(attempt -> attempt.getOutcome() == UsernameLookupAttempt.Outcome.FAILED)
                .count()
        ).isEqualTo(3);
        assertThat(lookupAttempts.count()).isEqualTo(6);
        String originalHash = account.getPasswordHash();
        mvc.perform(post("/auth/reset-password").with(csrf()).param("username", "newstaff")).andExpect(
            status().isNotFound()
        );
        assertThat(account.getPasswordHash()).isEqualTo(originalHash);
        mvc.perform(get("/login")).andExpect(
            content().string(org.hamcrest.Matchers.containsString("비밀번호 재설정 요청"))
        );
    }

    @Test
    void broadIpLimitStillStopsManyDifferentUsernameLookups() throws Exception {
        for (int i = 0; i < 30; i++) {
            mvc.perform(
                post("/auth/find-username")
                    .with(csrf())
                    .with(request -> {
                        request.setRemoteAddr("192.0.2.20");
                        return request;
                    })
                    .param("name", "없는직원" + i)
                    .param("phone", "01000000000")
            ).andExpect(status().isBadRequest());
        }
        mvc.perform(
            post("/auth/find-username")
                .with(csrf())
                .with(request -> {
                    request.setRemoteAddr("192.0.2.20");
                    return request;
                })
                .param("name", "새로운직원")
                .param("phone", "01011112222")
        )
            .andExpect(status().isTooManyRequests())
            .andExpect(
                jsonPath("$.message").value("조회 요청이 많습니다. 잠시 후 다시 시도하거나 관리자에게 문의해 주세요.")
            );
    }

    @Test
    void verificationEmailCanBeResentWithoutReusingOldLink() throws Exception {
        mvc.perform(
            post("/auth/register")
                .with(csrf())
                .param("name", "메일직원")
                .param("phone", "01022223333")
                .param("email", "mail@example.test")
                .param("username", "mailstaff")
                .param("password", "initial-pass")
        ).andExpect(status().isOk());
        mvc.perform(post("/auth/resend-verification").with(csrf()).param("email", "mail@example.test")).andExpect(
            status().isOk()
        );
        ArgumentCaptor<String> links = ArgumentCaptor.forClass(String.class);
        verify(verificationEmailSender, times(2)).send(
            org.mockito.ArgumentMatchers.eq("mail@example.test"),
            links.capture()
        );
        String oldToken = links.getAllValues().get(0).split("token=")[1];
        String newToken = links.getAllValues().get(1).split("token=")[1];
        mvc.perform(get("/auth/verify-email").param("token", oldToken)).andExpect(
            content().string(org.hamcrest.Matchers.containsString("이미 사용"))
        );
        mvc.perform(get("/auth/verify-email").param("token", newToken)).andExpect(
            content().string(org.hamcrest.Matchers.containsString("이메일 확인"))
        );
        mvc.perform(post("/auth/resend-verification").with(csrf()).param("email", "missing@example.test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("인증이 필요한 계정이면 이메일을 다시 보냈습니다."));
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
