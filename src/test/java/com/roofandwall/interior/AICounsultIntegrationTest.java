package com.roofandwall.interior;

import com.roofandwall.interior.aicounsult.AICounsultController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestExecutionListeners(listeners = {DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class}, inheritListeners = false)
@Transactional
class AICounsultIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired AICounsultController controller;

    private RestClient originalClient;
    private MockRestServiceServer aiServer;

    @BeforeEach
    void fixturesAndAiServer() {
        originalClient = (RestClient) ReflectionTestUtils.getField(controller, "aiClient");
        var builder = RestClient.builder().baseUrl("http://ai-service.test");
        aiServer = MockRestServiceServer.bindTo(builder).build();
        ReflectionTestUtils.setField(controller, "aiClient", builder.build());

        insertCustomerAndProject(11, 101, 1, false, "성수동 현장", "IN_PROGRESS");
        insertCustomerAndProject(22, 202, 2, false, "타업체 현장", "COMPLETED");
        insertCustomerAndProject(33, 303, 1, true, "보관 현장", "CANCELLED");
        insertCustomerAndProject(44, 404, 1, false, "신규 현장", "");
        jdbc.update("""
                INSERT INTO interior_cms.construction_projects
                (id, company_id, customer_id, name, address, status)
                VALUES (102, 1, 11, '동일 고객의 두 번째 현장', '비공개 주소', 'CONTRACTED')
                """);
        jdbc.update("""
                INSERT INTO interior_cms.staff_accounts
                (id, company_id, username, password_hash, display_name, role, active)
                VALUES (1, 1, 'ai-test-staff', 'unused', '직원', 'OFFICE', TRUE)
                """);
        jdbc.update("""
                INSERT INTO interior_cms.customer_consultation_items
                (company_id, customer_id, author_id, created_at, updated_at, sort_order, category, custom_category, content)
                VALUES (1, 11, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, '기타', '방음', '방음 공사 요청'),
                       (1, 11, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, '샷시', NULL, '창호 교체 요청')
                """);
    }

    @AfterEach
    void verifyRequestsAndRestoreClient() {
        try {
            aiServer.verify();
        } finally {
            ReflectionTestUtils.setField(controller, "aiClient", originalClient);
        }
    }

    @Test
    void listsOnlyAccessibleProjectsAndKeepsExplicitSelection() throws Exception {
        String html = mvc.perform(get("/aicounsult").param("projectId", "101").with(TestStaff.signedIn()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("projects", hasSize(3)))
                .andExpect(model().attribute("selectedProject", hasProperty("id", is(101L))))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("성수동 현장", "신규 현장", "동일 고객의 두 번째 현장")
                .doesNotContain("타업체 현장", "보관 현장");

        for (String projectId : new String[]{"202", "303", "999"}) {
            mvc.perform(get("/aicounsult").param("projectId", projectId).with(TestStaff.signedIn()))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void sendsDatabaseContextAndCustomerConsultationsWithoutCallerOverridesOrPersonalFields() throws Exception {
        jdbc.update("UPDATE interior_cms.construction_projects SET status = 'ESTIMATING' WHERE id = 101");
        expectProjectRequest(101, "성수동 현장", "견적");
        expectProjectRequest(102, "동일 고객의 두 번째 현장", "계약");

        for (String projectId : new String[]{"101", "102"}) {
            mvc.perform(post("/aicounsult/chat").with(TestStaff.signedIn()).with(csrf())
                            .param("message", "현재 상황과 상담 요청을 알려줘")
                            .param("projectId", projectId)
                            .param("companyId", "2")
                            .param("context", "고객은 공사를 취소했다")
                            .param("project.status", "취소"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("chatAnswer", "저장된 상담에는 창호 교체와 방음 공사 요청이 있습니다."))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("창호 교체와 방음 공사 요청")));
        }
    }

    @Test
    void rejectsOtherCompanyArchivedAndMissingProjectsBeforeCallingAi() throws Exception {
        for (String projectId : new String[]{"202", "303", "999"}) {
            mvc.perform(post("/aicounsult/chat").with(TestStaff.signedIn()).with(csrf())
                            .param("projectId", projectId).param("companyId", "2").param("message", "진행 상황"))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void sendsEmptyConsultationsAndUnspecifiedStatusForExistingIncompleteData() throws Exception {
        aiServer.expect(requestTo("http://ai-service.test/chat"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.content().json("""
                        {"message":"진행 상황", "project":{"id":404,"name":"신규 현장","status":"미지정","consultations":[]}}
                        """, JsonCompareMode.STRICT))
                .andRespond(withSuccess("{\"answer\":\"진행 상태와 상담 기록을 확인해 주세요.\"}", MediaType.APPLICATION_JSON));
        mvc.perform(post("/aicounsult/chat").with(TestStaff.signedIn()).with(csrf())
                        .param("projectId", "404").param("message", "진행 상황"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("chatAnswer", "진행 상태와 상담 기록을 확인해 주세요."));
    }

    @Test
    void supportsGeneralQuestionWithoutProjectContext() throws Exception {
        aiServer.expect(requestTo("http://ai-service.test/chat"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.content().json("""
                        {"message":"도배 전 준비는?", "project":null}
                        """, JsonCompareMode.STRICT))
                .andRespond(withSuccess("{\"answer\":\"벽면 상태를 확인하세요.\"}", MediaType.APPLICATION_JSON));
        mvc.perform(post("/aicounsult/chat").with(TestStaff.signedIn()).with(csrf())
                        .param("message", "도배 전 준비는?"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("chatAnswer", "벽면 상태를 확인하세요."));
    }

    @Test
    void requiresLoginAndCsrfBeforeCallingAi() throws Exception {
        mvc.perform(post("/aicounsult/chat").with(csrf()).param("projectId", "101").param("message", "질문"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        mvc.perform(post("/aicounsult/chat").with(TestStaff.signedIn())
                        .param("projectId", "101").param("message", "질문"))
                .andExpect(status().isForbidden());
    }

    @Test
    void preservesSelectedProjectAndQuestionWhenValidationOrAiRequestFails() throws Exception {
        for (String message : new String[]{"   ", "가".repeat(1001)}) {
            mvc.perform(post("/aicounsult/chat").with(TestStaff.signedIn()).with(csrf())
                            .param("projectId", "101").param("message", message))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("chatError"))
                    .andExpect(model().attribute("selectedProject", hasProperty("id", is(101L))))
                    .andExpect(model().attribute("chatQuestion", message.strip()));
        }

        aiServer.expect(requestTo("http://ai-service.test/chat"))
                .andRespond(withStatus(HttpStatus.GATEWAY_TIMEOUT));
        mvc.perform(post("/aicounsult/chat").with(TestStaff.signedIn()).with(csrf())
                        .param("projectId", "101").param("message", "진행 상황"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("chatError"))
                .andExpect(model().attribute("projects", hasSize(3)))
                .andExpect(model().attribute("selectedProject", hasProperty("id", is(101L))))
                .andExpect(model().attribute("chatQuestion", "진행 상황"));
    }

    private void expectProjectRequest(long projectId, String name, String status) {
        aiServer.expect(requestTo("http://ai-service.test/chat"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockRestRequestMatchers.content().json("""
                        {
                          "message":"현재 상황과 상담 요청을 알려줘",
                          "project":{
                            "id":%d,"name":"%s","status":"%s",
                            "consultations":[
                              {"category":"샷시","customCategory":null,"content":"창호 교체 요청"},
                              {"category":"기타","customCategory":"방음","content":"방음 공사 요청"}
                            ]
                          }
                        }
                        """.formatted(projectId, name, status), JsonCompareMode.STRICT))
                .andRespond(withSuccess("{\"answer\":\"저장된 상담에는 창호 교체와 방음 공사 요청이 있습니다.\"}",
                        MediaType.APPLICATION_JSON));
    }

    private void insertCustomerAndProject(long customerId, long projectId, long companyId,
                                          boolean archived, String projectName, String status) {
        jdbc.update("INSERT INTO interior_cms.customers (id, company_id, name, phone, archived) VALUES (?, ?, ?, ?, ?)",
                customerId, companyId, "비공개 고객", "010-9876-5432", archived);
        jdbc.update("""
                INSERT INTO interior_cms.construction_projects
                (id, company_id, customer_id, name, address, status)
                VALUES (?, ?, ?, ?, '비공개 주소', ?)
                """, projectId, companyId, customerId, projectName, status);
    }
}
