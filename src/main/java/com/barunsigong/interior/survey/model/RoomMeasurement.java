package com.barunsigong.interior.survey.model;

import com.barunsigong.interior.common.model.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** 후속 기능의 데이터 모델. 변경 규칙은 해당 서비스 구현 시 적용합니다. */
@Entity
@Table(name = "room_measurements")
public class RoomMeasurement extends BaseEntity {

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long surveyorId;

    @Column(nullable = false)
    private Integer revision;

    @Column(nullable = false, length = 100)
    private String roomName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal perimeter;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal height;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal openingArea;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal ceilingArea;

    @Column(nullable = false)
    private boolean includeCeiling;

    @Column(length = 1000)
    private String surfaceCondition;

    protected RoomMeasurement() {}

    public RoomMeasurement(
        Long companyId,
        Long projectId,
        Long surveyorId,
        Integer revision,
        String roomName,
        BigDecimal perimeter,
        BigDecimal height,
        BigDecimal openingArea,
        BigDecimal ceilingArea,
        boolean includeCeiling,
        String surfaceCondition
    ) {
        super(companyId);
        this.projectId = projectId;
        this.surveyorId = surveyorId;
        this.revision = revision;
        this.roomName = roomName;
        this.perimeter = perimeter;
        this.height = height;
        this.openingArea = openingArea;
        this.ceilingArea = ceilingArea;
        this.includeCeiling = includeCeiling;
        this.surfaceCondition = surfaceCondition;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getSurveyorId() {
        return surveyorId;
    }

    public Integer getRevision() {
        return revision;
    }

    public String getRoomName() {
        return roomName;
    }

    public BigDecimal getPerimeter() {
        return perimeter;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public BigDecimal getOpeningArea() {
        return openingArea;
    }

    public BigDecimal getCeilingArea() {
        return ceilingArea;
    }

    public boolean isIncludeCeiling() {
        return includeCeiling;
    }

    public String getSurfaceCondition() {
        return surfaceCondition;
    }
}
