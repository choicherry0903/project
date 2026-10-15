package com.roofandwall.interior;

import com.roofandwall.interior.auth.service.StaffPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestExecutionListeners(listeners = {
        DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class
}, inheritListeners = false)
@Transactional
class AuthIntegrationTest {
    private static final String PASSWORD = "TestPass!2026";
    private static final String HASH = new BCryptPasswordEncoder(4).encode(PASSWORD);

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void accounts() {
        jdbc.update("""
                INSERT INTO interior_cms.staff_accounts
                (id, company_id, username, password_hash, display_name, role, active, email, email_verified_at)
                VALUES (1, 2, 'office', ?, '테스트 직원', 'OFFICE', TRUE, NULL, NULL),
                       (2, 2, 'inactive', ?, '비활성 직원', 'OFFICE', FALSE, NULL, NULL),
                       (3, 2, 'unverified', ?, '미인증 직원', 'OFFICE', TRUE, 'unverified@example.com', NULL),
                       (4, 2, 'verified', ?, '인증 직원', 'OFFICE', TRUE, 'verified@example.com', CURRENT_TIMESTAMP)
                """, HASH, HASH, HASH, HASH);
    }

    @Test
    void loginScreenIsPublicAndHasCsrfAndFeedback() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(containsString("autocomplete=\"current-password\"")));
        mvc.perform(get("/login?error"))
                .andExpect(content().string(containsString("아이디 또는 비밀번호를 확인")));
        mvc.perform(get("/login?logout"))
                .andExpect(content().string(containsString("로그아웃되었습니다")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/", "/customers", "/customers/regform", "/customers/detail", "/aicounsult"})
    void businessScreensRequireLogin(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void existingBcryptAccountLogsInCaseInsensitivelyAndScopesProjectsByStaffCompany() throws Exception {
        MockHttpSession session = (MockHttpSession) mvc.perform(formLogin().user(" OFFICE ").password(PASSWORD))
                .andExpect(authenticated().withUsername("office"))
                .andExpect(redirectedUrl("/customers"))
                .andReturn().getRequest().getSession(false);
        SecurityContext context = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
        assertThat(((StaffPrincipal) context.getAuthentication().getPrincipal()).getCompanyId()).isEqualTo(2);
        jdbc.update("""
                INSERT INTO interior_cms.customers (id, company_id, name, phone, archived)
                VALUES (1, 1, '다른 업체 고객', '010-1111-1111', FALSE),
                       (2, 2, '우리 업체 고객', '010-2222-2222', FALSE)
                """);
        jdbc.update("""
                INSERT INTO interior_cms.construction_projects
                (id, company_id, customer_id, name, address, status)
                VALUES (1, 1, 1, '다른 업체 현장', '서울', 'CONSULTATION'),
                       (2, 2, 2, '우리 업체 현장', '서울', 'CONSULTATION')
                """);
        String html = mvc.perform(get("/customers").session(session))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(html).contains("우리 업체 현장", "테스트 직원").doesNotContain("다른 업체 현장");
        mvc.perform(get("/login").session(session)).andExpect(redirectedUrl("/customers"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"missing", "inactive", "unverified"})
    void rejectsUnknownDisabledAndUnverifiedAccounts(String username) throws Exception {
        mvc.perform(formLogin().user(username).password(PASSWORD))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void verifiedAccountCanLoginButIncorrectPasswordCannot() throws Exception {
        mvc.perform(formLogin().user("verified").password(PASSWORD)).andExpect(authenticated());
        mvc.perform(formLogin().user("office").password("incorrect"))
                .andExpect(unauthenticated()).andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void csrfIsRequiredAndLogoutInvalidatesSession() throws Exception {
        mvc.perform(post("/login").param("username", "office").param("password", PASSWORD))
                .andExpect(status().isForbidden());
        MockHttpSession session = (MockHttpSession) mvc.perform(formLogin().user("office").password(PASSWORD))
                .andExpect(authenticated()).andReturn().getRequest().getSession(false);
        mvc.perform(post("/logout").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/logout").session(session).with(csrf()))
                .andExpect(redirectedUrl("/login?logout")).andExpect(unauthenticated());
        assertThat(session.isInvalid()).isTrue();
    }
}
