package com.roofandwall.interior.estimate.service;

import com.roofandwall.interior.auth.service.CurrentStaff;
import com.roofandwall.interior.common.exception.ResourceNotFoundException;
import com.roofandwall.interior.estimate.dto.EstimateForm;
import com.roofandwall.interior.estimate.model.*;
import com.roofandwall.interior.estimate.repository.*;
import com.roofandwall.interior.project.repository.ConstructionProjectRepository;
import com.roofandwall.interior.project.model.ConstructionProject;
import com.roofandwall.interior.material.repository.MaterialRepository;
import com.roofandwall.interior.material.model.Material;
import java.math.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('ADMIN','OFFICE')")
public class EstimateService {
    private final EstimateRepository estimates;
    private final EstimateItemRepository items;
    private final ConstructionProjectRepository projects;
    private final MaterialRepository materials;
    private final CurrentStaff staff;
    private final EstimateCalculator calculator;
    public EstimateService(EstimateRepository estimates, EstimateItemRepository items,
            ConstructionProjectRepository projects, MaterialRepository materials,
            CurrentStaff staff, EstimateCalculator calculator) {
        this.estimates = estimates; this.items = items; this.projects = projects;
        this.materials = materials; this.staff = staff; this.calculator = calculator;
    }
    public Estimate get(Long id) {
        return estimates.findByIdAndCompanyIdAndArchivedFalse(id, staff.companyId())
            .orElseThrow(ResourceNotFoundException::new);
    }
    public ConstructionProject project(Long id) {
        return projects.findByIdAndCompanyId(id, staff.companyId()).orElseThrow(ResourceNotFoundException::new);
    }
    public List<ConstructionProject> projectChoices() { return projects.findByCompanyIdOrderByNameAsc(staff.companyId()); }
    public List<Material> materialChoices() { return materials.findByCompanyIdAndActiveTrueOrderByNameAsc(staff.companyId()); }
    public List<EstimateItem> items(Long id) {
        get(id);
        return items.findByCompanyIdAndEstimateIdOrderByIdAsc(staff.companyId(), id);
    }
    public Page<Estimate> search(Long projectId, EstimateStatus status, String keyword, int page) {
        if (projectId != null) project(projectId);
        return estimates.search(staff.companyId(), projectId, status, keyword.trim(),
            PageRequest.of(Math.max(0, page), 20, Sort.by("updatedAt").descending().and(Sort.by("id").descending())));
    }
    public EstimateForm form(Long id) {
        var e = get(id);
        editable(e);
        var f = new EstimateForm();
        f.setProjectId(e.getProjectId()); f.setVersion(e.getVersion());
        if (e.getRequestKey() != null) f.setRequestKey(e.getRequestKey());
        f.setValidUntil(e.getValidUntil()); f.setScopeOfWork(e.getScopeOfWork());
        f.setExcludedWork(e.getExcludedWork()); f.setDiscount(e.getDiscount()); f.setTaxRate(e.getTaxRate());
        f.setItems(items(id).stream().map(i -> {
            var line = new EstimateForm.Item();
            line.setCategory(i.getCategory()); line.setName(i.getName()); line.setUnit(i.getUnit());
            line.setQuantity(i.isQuantityMissing() ? null : i.getQuantity());
            line.setUnitPrice(i.isUnitPriceMissing() ? null : i.getUnitPrice());
            line.setFreeItemReason(i.getFreeItemReason()); return line;
        }).collect(java.util.stream.Collectors.toCollection(ArrayList::new)));
        return f;
    }
    private void editable(Estimate e) {
        if (e.getStatus() != EstimateStatus.DRAFT) throw new IllegalArgumentException("초안 견적만 수정하거나 삭제할 수 있습니다.");
    }
    @Transactional
    public Estimate save(Long id, EstimateForm f) {
        var project = projects.lockForEstimate(f.getProjectId(), staff.companyId()).orElseThrow(ResourceNotFoundException::new);
        Estimate e;
        if (id == null) {
            var previous = estimates.findByCompanyIdAndRequestKey(staff.companyId(), f.getRequestKey());
            if (previous.isPresent()) {
                if (previous.get().isArchived()) throw new IllegalArgumentException("삭제된 견적의 저장 요청입니다. 새 견적을 작성해 주세요.");
                return previous.get();
            }
            e = new Estimate(staff.companyId(), project.getId(), estimates.latestRevision(staff.companyId(), project.getId()) + 1,
                EstimateStatus.DRAFT, null, null, null, null, 0L, BigDecimal.ZERO, 0L, 0L, 0L, null, null);
            e.setRequestKey(f.getRequestKey());
        } else {
            e = estimates.lockDraft(id, staff.companyId()).orElseThrow(ResourceNotFoundException::new);
            editable(e);
            if (!e.getProjectId().equals(f.getProjectId())) throw new IllegalArgumentException("기존 견적의 현장은 변경할 수 없습니다.");
            if (e.getVersion() != f.getVersion()) throw new IllegalArgumentException("다른 사용자가 수정했습니다. 새로고침 후 다시 확인해 주세요.");
        }
        var priced = f.getItems().stream().filter(i -> i.getQuantity() != null && i.getUnitPrice() != null).toList();
        EstimateCalculator.Total total;
        if (priced.isEmpty()) {
            if (f.getDiscount() != 0) throw new IllegalArgumentException("할인은 항목 합계 이내여야 합니다.");
            total = new EstimateCalculator.Total(0, 0, 0, 0, 0);
        } else {
            total = calculator.calculate(priced.stream().map(i -> new EstimateCalculator.Line(i.getQuantity(), i.getUnitPrice())).toList(), f.getDiscount(), f.getTaxRate());
        }
        if (f.getDisplayedTotal() != null && f.getDisplayedTotal() != total.totalAmount())
            throw new IllegalArgumentException("표시 금액과 계산 결과가 다릅니다. 수량·단가를 확인해 주세요.");
        e.updateDraft(f.getValidUntil(), f.getScopeOfWork(), f.getExcludedWork(), total.discount(), f.getTaxRate(),
            total.supplyAmount(), total.taxAmount(), total.totalAmount());
        e = estimates.saveAndFlush(e);
        if (id != null) {
            items.deleteAll(items.findByCompanyIdAndEstimateIdOrderByIdAsc(staff.companyId(), id));
            items.flush();
        }
        for (var line : f.getItems()) {
            long amount = line.getQuantity() == null || line.getUnitPrice() == null ? 0L : line.getQuantity()
                .multiply(BigDecimal.valueOf(line.getUnitPrice())).setScale(0, RoundingMode.HALF_UP).longValueExact();
            var item = new EstimateItem(staff.companyId(), e.getId(), line.getCategory(),
                line.getName() == null ? "" : line.getName().trim(), line.getUnit() == null ? "" : line.getUnit().trim(),
                line.getQuantity() == null ? BigDecimal.ZERO : line.getQuantity(),
                line.getUnitPrice() == null ? 0L : line.getUnitPrice(), amount, line.getFreeItemReason());
            item.markIncomplete(line.getQuantity() == null, line.getUnitPrice() == null);
            items.save(item);
        }
        items.flush();
        return e;
    }
    @Transactional
    public void delete(Long id, long version) {
        var e = estimates.lockDraft(id, staff.companyId()).orElseThrow(ResourceNotFoundException::new);
        editable(e);
        if (e.getVersion() != version) throw new IllegalArgumentException("견적이 변경되었습니다. 다시 확인한 뒤 삭제해 주세요.");
        e.archive();
        estimates.saveAndFlush(e);
    }
}
