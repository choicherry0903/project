package com.barunsigong.interior.survey.service;

import com.barunsigong.interior.auth.service.CurrentStaff;
import com.barunsigong.interior.common.exception.ResourceNotFoundException;
import com.barunsigong.interior.project.service.ConstructionProjectService;
import com.barunsigong.interior.survey.dto.RoomMeasurementForm;
import com.barunsigong.interior.survey.model.RoomMeasurement;
import com.barunsigong.interior.survey.repository.RoomMeasurementRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('ADMIN','OFFICE')")
public class SurveyService {

    public record RoomView(RoomMeasurement measurement, BigDecimal wallArea, BigDecimal totalArea) {}

    private final RoomMeasurementRepository measurements;
    private final ConstructionProjectService projects;
    private final MeasurementCalculator calculator;
    private final CurrentStaff staff;

    public SurveyService(
        RoomMeasurementRepository measurements,
        ConstructionProjectService projects,
        MeasurementCalculator calculator,
        CurrentStaff staff
    ) {
        this.measurements = measurements;
        this.projects = projects;
        this.calculator = calculator;
        this.staff = staff;
    }

    public List<RoomView> latest(Long projectId) {
        projects.get(projectId);
        return measurements.findLatestByProject(staff.companyId(), projectId).stream().map(this::view).toList();
    }

    public RoomMeasurement current(Long projectId, Long id) {
        var measurement = get(projectId, id);
        var latest = measurements.findTopByCompanyIdAndProjectIdAndRoomKeyOrderByRevisionDesc(
            staff.companyId(), projectId, measurement.getRoomKey()
        ).orElseThrow(ResourceNotFoundException::new);
        if (!latest.getId().equals(id)) throw new ResourceNotFoundException();
        return measurement;
    }

    public List<RoomView> history(Long projectId, Long id) {
        var measurement = get(projectId, id);
        return measurements.findByCompanyIdAndProjectIdAndRoomKeyOrderByRevisionDesc(
            staff.companyId(), projectId, measurement.getRoomKey()
        ).stream().map(this::view).toList();
    }

    @Transactional
    public RoomMeasurement save(Long projectId, Long id, RoomMeasurementForm form) {
        projects.get(projectId);
        calculator.wallArea(form.getPerimeter(), form.getHeight(), form.getOpeningArea());
        var ceiling = form.getCeilingArea();
        if (ceiling == null || ceiling.signum() < 0 || ceiling.scale() > 2 ||
            ceiling.compareTo(new BigDecimal("999999.99")) > 0) {
            throw new IllegalArgumentException("천장 면적은 0 이상, 소수점 2자리 이내로 입력해 주세요.");
        }

        String roomKey = UUID.randomUUID().toString();
        int revision = 1;
        if (id != null) {
            var previous = measurements.findByIdAndCompanyIdAndProjectId(id, staff.companyId(), projectId)
                .orElseThrow(ResourceNotFoundException::new);
            var latest = measurements.findTopByCompanyIdAndProjectIdAndRoomKeyOrderByRevisionDesc(
                staff.companyId(), projectId, previous.getRoomKey()
            ).orElseThrow(ResourceNotFoundException::new);
            if (!latest.getId().equals(id)) throw new IllegalArgumentException(
                "다른 사용자가 실측을 수정했습니다. 최신 기록을 확인해 주세요."
            );
            roomKey = previous.getRoomKey();
            revision = Math.addExact(previous.getRevision(), 1);
        }

        return measurements.saveAndFlush(new RoomMeasurement(
            staff.companyId(), projectId, staff.get().getAccountId(), roomKey, revision,
            form.getRoomName().trim(), form.getPerimeter(), form.getHeight(), form.getOpeningArea(),
            ceiling, form.isIncludeCeiling(), form.getSurfaceCondition()
        ));
    }

    private RoomMeasurement get(Long projectId, Long id) {
        projects.get(projectId);
        return measurements.findByIdAndCompanyId(id, staff.companyId())
            .filter(measurement -> measurement.getProjectId().equals(projectId))
            .orElseThrow(ResourceNotFoundException::new);
    }

    private RoomView view(RoomMeasurement measurement) {
        var wallArea = calculator.wallArea(
            measurement.getPerimeter(), measurement.getHeight(), measurement.getOpeningArea()
        );
        var totalArea = measurement.isIncludeCeiling() ? wallArea.add(measurement.getCeilingArea()) : wallArea;
        return new RoomView(measurement, wallArea, totalArea);
    }
}
