package com.barunsigong.interior.project.service;
import com.barunsigong.interior.auth.service.CurrentStaff;
import com.barunsigong.interior.common.exception.ResourceNotFoundException;
import com.barunsigong.interior.customer.service.CustomerService;
import com.barunsigong.interior.project.dto.ConstructionProjectForm;
import com.barunsigong.interior.project.model.*;
import com.barunsigong.interior.project.repository.ConstructionProjectRepository;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @Transactional(readOnly=true) @PreAuthorize("hasAnyRole('ADMIN','OFFICE')")
public class ConstructionProjectService {
    private final ConstructionProjectRepository projects;
    private final CustomerService customers;
    private final CurrentStaff staff;
    public ConstructionProjectService(ConstructionProjectRepository projects,CustomerService customers,CurrentStaff staff) {
        this.projects=projects; this.customers=customers; this.staff=staff;
    }
    public ConstructionProject get(Long id) { return projects.findByIdAndCompanyId(id,staff.companyId()).orElseThrow(ResourceNotFoundException::new); }
    public long count() { return projects.countByCompanyId(staff.companyId()); }
    public List<ConstructionProject> forCustomer(Long customerId) {
        customers.get(customerId); return projects.findByCompanyIdAndCustomerIdOrderByUpdatedAtDesc(staff.companyId(),customerId);
    }
    public Page<ConstructionProject> search(String keyword,ProjectStatus status,int page) {
        return projects.search(staff.companyId(),keyword.trim(),status,PageRequest.of(Math.max(0,page),20,Sort.by("updatedAt").descending()));
    }
    @Transactional public ConstructionProject save(Long id,ConstructionProjectForm form) {
        customers.get(form.getCustomerId());
        if (id==null) {
            var previous=projects.findByRequestKeyAndCompanyId(form.getRequestKey(),staff.companyId());
            if (previous.isPresent()) return previous.get();
            return projects.saveAndFlush(new ConstructionProject(staff.companyId(),form.getCustomerId(),form.getName(),form.getAddress(),
                form.getDetailAddress(),form.getConstructionType(),form.getDesiredStartDate(),form.getHousingType(),form.isOccupied(),
                form.getSiteNotes(),ProjectStatus.CONSULTATION,false,null,form.getRequestKey()));
        }
        var project=get(id);
        if (project.getVersion()!=form.getVersion()) throw new IllegalArgumentException("다른 사용자가 변경했습니다. 입력 내용을 복사한 뒤 새로고침해 주세요.");
        if (project.getStatus()!=ProjectStatus.CONSULTATION && !project.getCustomerId().equals(form.getCustomerId()))
            throw new IllegalArgumentException("진행 중 현장의 고객은 변경할 수 없습니다.");
        project.updateDetails(form.getCustomerId(),form.getName(),form.getAddress(),form.getDetailAddress(),form.getConstructionType(),
            form.getDesiredStartDate(),form.getHousingType(),form.isOccupied(),form.getSiteNotes());
        return projects.saveAndFlush(project);
    }
}
