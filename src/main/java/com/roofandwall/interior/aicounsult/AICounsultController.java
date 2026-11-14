package com.roofandwall.interior.aicounsult;

import java.time.Duration;
import java.util.List;

import com.roofandwall.interior.auth.service.StaffPrincipal;
import com.roofandwall.interior.customer.dto.CustomerItem;
import com.roofandwall.interior.customer.service.CustomerQueryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Controller
public class AICounsultController {

    private static final String VIEW = "ai-counsult/ai-counsult";

    private final CustomerQueryService customerQueryService;
    private final RestClient aiClient;

    public AICounsultController(CustomerQueryService customerQueryService,
                               @Value("${ai.service.base-url}") String baseUrl) {
        this.customerQueryService = customerQueryService;
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(Duration.ofSeconds(45));
        aiClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/aicounsult";
    }

    @GetMapping("/aicounsult")
    public String index(@RequestParam(required = false) Long projectId,
                        @AuthenticationPrincipal StaffPrincipal staff, Model model) {
        preparePage(projectId, staff, model);
        return VIEW;
    }

    @PostMapping("/aicounsult/chat")
    public String chat(@RequestParam(defaultValue = "") String message,
                       @RequestParam(required = false) Long projectId,
                       @AuthenticationPrincipal StaffPrincipal staff, Model model) {
        CustomerItem selectedProject = preparePage(projectId, staff, model);
        String question = message.strip();
        model.addAttribute("chatQuestion", question);
        if (question.isEmpty() || question.length() > 1000) {
            model.addAttribute("chatError", "질문을 1~1,000자로 입력해 주세요.");
            return VIEW;
        }

        ProjectContext project = null;
        if (selectedProject != null) {
            // 모델에는 답변에 필요한 현장 상태와 고객 공통 상담 항목만 전달한다.
            project = new ProjectContext(selectedProject.getId(), selectedProject.getProjectName(),
                    selectedProject.statusLabel(), selectedProject.getConsultationItems().stream()
                    .map(item -> new ConsultationContext(item.getCategory(), item.getCustomCategory(), item.getContent()))
                    .toList());
        }
        try {
            ChatResponse response = aiClient.post().uri("/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ChatRequest(question, project))
                    .retrieve().body(ChatResponse.class);
            if (response == null || response.answer() == null || response.answer().isBlank()) {
                model.addAttribute("chatError", "AI 서비스에서 응답을 받지 못했습니다.");
            } else {
                model.addAttribute("chatAnswer", response.answer());
            }
        } catch (RestClientResponseException exception) {
            String errorMessage = switch (exception.getStatusCode().value()) {
                case 429 -> "AI 사용 한도에 도달했습니다. API 크레딧과 사용량을 확인한 뒤 다시 시도해 주세요.";
                case 502 -> "AI 요청을 처리하지 못했습니다. API 키와 모델 설정을 확인해 주세요.";
                case 503 -> "AI 서비스 설정이 필요합니다. 서버의 API 키 설정을 확인해 주세요.";
                case 504 -> "AI 응답 시간이 초과되었습니다. 잠시 후 다시 질문해 주세요.";
                default -> "AI 서비스에서 요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.";
            };
            model.addAttribute("chatError", errorMessage);
        } catch (RestClientException exception) {
            model.addAttribute("chatError", "AI 서비스에 연결하지 못했습니다. 잠시 후 다시 시도해 주세요.");
        }
        return VIEW;
    }

    private CustomerItem preparePage(Long projectId, StaffPrincipal staff, Model model) {
        // 업체 ID는 로그인 세션에서 가져오며, 선택한 현장도 같은 범위로 다시 조회한다.
        CustomerItem selectedProject = projectId == null ? null
                : customerQueryService.requireDetail(staff.getCompanyId(), projectId);
        model.addAttribute("projects", customerQueryService.findAll(staff.getCompanyId()));
        model.addAttribute("selectedProject", selectedProject);
        return selectedProject;
    }

    private record ChatRequest(String message, ProjectContext project) {}

    private record ProjectContext(long id, String name, String status, List<ConsultationContext> consultations) {}

    private record ConsultationContext(String category, String customCategory, String content) {}

    private record ChatResponse(String answer) {}
}
