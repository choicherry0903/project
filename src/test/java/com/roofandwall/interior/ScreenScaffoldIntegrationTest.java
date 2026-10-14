package com.roofandwall.interior;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 전체 앱을 기동해 템플릿 렌더링과 화면 전용 범위를 확인한다. DB/API는 필요 없다. */
@SpringBootTest
@AutoConfigureMockMvc
@TestExecutionListeners(listeners = DependencyInjectionTestExecutionListener.class, inheritListeners = false)
class ScreenScaffoldIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void homeOpensProjectList() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects"));
    }

    @ParameterizedTest
    @CsvSource({
            "/projects, project/list, 고객·현장 목록",
            "/projects/detail, project/detail, 현장 상세",
            "/consultations/ai, consultation/ai, AI 상담 정리"
    })
    void rendersThreeScreensWithSharedNavigation(String path, String view, String title) throws Exception {
        String html = mvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(view().name(view))
                .andExpect(content().string(containsString("<h1>" + title + "</h1>")))
                .andExpect(content().string(containsString("지붕과벽")))
                .andExpect(content().string(containsString("화면 준비 중")))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);

        String navigation = html.substring(html.indexOf("<nav"), html.indexOf("</nav>"));
        assertThat(navigation).contains("href=\"/projects\"", "href=\"/projects/detail\"", "href=\"/consultations/ai\"");
        assertThat(navigation.split("<a ", -1)).hasSize(4);
        assertThat(navigation.split("aria-current=\"page\"", -1)).hasSize(2);
        assertThat(html).doesNotContain("th:replace", "<form", "<script");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/customers", "/materials", "/survey", "/estimates", "/login", "/settings"})
    void oldScreensAreRemoved(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/projects", "/projects/detail", "/consultations/ai"})
    void noBusinessSubmissionEndpointsExist(String path) throws Exception {
        mvc.perform(post(path)).andExpect(status().isMethodNotAllowed());
    }

    @Test
    void stylesheetIsServed() throws Exception {
        mvc.perform(get("/css/app.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(".app-shell")));
    }
}
