package com.barunsigong.interior.dashboard.controller;
import com.barunsigong.interior.customer.service.CustomerService;
import com.barunsigong.interior.project.service.ConstructionProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.time.*;
@Controller
public class DashboardController {
    private final CustomerService customers;
    private final ConstructionProjectService projects;
    public DashboardController(CustomerService customers,ConstructionProjectService projects) { this.customers=customers; this.projects=projects; }
    @GetMapping("/dashboard") public String dashboard(Model model) {
        model.addAttribute("today",LocalDate.now(ZoneId.of("Asia/Seoul")));
        model.addAttribute("customerCount",customers.count()); model.addAttribute("projectCount",projects.count());
        model.addAttribute("recentProjects",projects.search("",null,0).getContent()); return "dashboard/index";
    }
}
