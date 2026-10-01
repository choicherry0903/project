package com.barunsigong.interior;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.barunsigong.interior.auth.model.Role;
import com.barunsigong.interior.auth.model.StaffAccount;
import com.barunsigong.interior.auth.repository.StaffAccountRepository;
import com.barunsigong.interior.auth.service.StaffPrincipal;
import com.barunsigong.interior.auth.service.VerificationEmailSender;
import com.barunsigong.interior.customer.model.Customer;
import com.barunsigong.interior.customer.repository.CustomerRepository;
import com.barunsigong.interior.project.model.ConstructionProject;
import com.barunsigong.interior.project.model.ProjectStatus;
import com.barunsigong.interior.project.repository.ConstructionProjectRepository;
import com.barunsigong.interior.survey.repository.RoomMeasurementRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SurveyIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired StaffAccountRepository accounts;
    @Autowired CustomerRepository customers;
    @Autowired ConstructionProjectRepository projects;
    @Autowired RoomMeasurementRepository measurements;
    @MockitoBean VerificationEmailSender verificationEmailSender;

    StaffPrincipal admin;
    ConstructionProject project;

    @BeforeEach
    void setup() {
        admin = new StaffPrincipal(accounts.findByUsernameIgnoreCase("test-admin").orElseThrow());
        project = project(1L);
    }

    @Test
    void createEditAndReviewRoomHistory() throws Exception {
        String path = "/projects/" + project.getId() + "/survey";
        mvc.perform(get(path).with(user(admin)))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("등록된 실측 공간이 없습니다")));
        mvc.perform(get(path + "/new").with(user(admin)))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("공간 실측 등록")));
        mvc.perform(post(path).with(user(admin)).with(csrf())
            .param("roomName", "거실")
            .param("perimeter", "10.00")
            .param("height", "2.50")
            .param("openingArea", "2.00")
            .param("ceilingArea", "12.00")
            .param("includeCeiling", "true")
            .param("surfaceCondition", "기존 벽지 제거"))
            .andExpect(redirectedUrl(path));

        var original = measurements.findLatestByProject(1L, project.getId()).getFirst();
        assertThat(original.getRevision()).isEqualTo(1);
        assertThat(original.getSurveyorId()).isEqualTo(admin.getAccountId());
        mvc.perform(get(path).with(user(admin)))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("23.00")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("35.00")));
        mvc.perform(get(path + "/" + original.getId() + "/edit").with(user(admin)))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("기존 벽지 제거")));

        mvc.perform(post(path + "/" + original.getId()).with(user(admin)).with(csrf())
            .param("roomName", "거실·복도")
            .param("perimeter", "11.00")
            .param("height", "2.50")
            .param("openingArea", "2.00")
            .param("ceilingArea", "12.00"))
            .andExpect(redirectedUrl(path));
        var latest = measurements.findLatestByProject(1L, project.getId()).getFirst();
        assertThat(latest.getRevision()).isEqualTo(2);
        assertThat(latest.getRoomKey()).isEqualTo(original.getRoomKey());
        assertThat(measurements.count()).isEqualTo(2);
        mvc.perform(get(path + "/" + original.getId() + "/edit").with(user(admin)))
            .andExpect(status().isNotFound());
        mvc.perform(get(path + "/" + latest.getId() + "/history").with(user(admin)))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("v1")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("v2")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("기존 벽지 제거")));

        mvc.perform(post(path + "/" + original.getId()).with(user(admin)).with(csrf())
            .param("roomName", "오래된 수정")
            .param("perimeter", "12")
            .param("height", "2.50")
            .param("openingArea", "0")
            .param("ceilingArea", "0"))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasErrors("form"));
        assertThat(measurements.count()).isEqualTo(2);
    }

    @Test
    void invalidDimensionsAndMissingCsrfDoNotSave() throws Exception {
        String path = "/projects/" + project.getId() + "/survey";
        mvc.perform(post(path).with(user(admin))
            .param("roomName", "침실")
            .param("perimeter", "10")
            .param("height", "2.5")
            .param("openingArea", "1")
            .param("ceilingArea", "0"))
            .andExpect(status().isForbidden());
        mvc.perform(post(path).with(user(admin)).with(csrf())
            .param("roomName", "침실")
            .param("perimeter", "10")
            .param("height", "2.5")
            .param("openingArea", "26")
            .param("ceilingArea", "0"))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasErrors("form"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("개구부 면적은 전체 벽면보다 클 수 없습니다")));
        assertThat(measurements.count()).isZero();
    }

    @Test
    void otherCompanyAndWorkerCannotAccessSurvey() throws Exception {
        var foreignProject = project(2L);
        mvc.perform(get("/projects/" + foreignProject.getId() + "/survey").with(user(admin)))
            .andExpect(status().isNotFound());
        var worker = accounts.saveAndFlush(new StaffAccount(1L, "survey-worker@example.test", "unused", "작업자", Role.WORKER));
        mvc.perform(get("/projects/" + project.getId() + "/survey").with(user(new StaffPrincipal(worker))))
            .andExpect(status().isForbidden());
    }

    private ConstructionProject project(Long companyId) {
        var customer = customers.saveAndFlush(new Customer(
            companyId, "실측 고객", "01012345678", null, null, UUID.randomUUID().toString(), null, false
        ));
        return projects.saveAndFlush(new ConstructionProject(
            companyId, customer.getId(), "실측 현장", "서울 테스트 주소", null, "도배", null, null,
            false, null, ProjectStatus.CONSULTATION, false, null, UUID.randomUUID().toString()
        ));
    }
}
