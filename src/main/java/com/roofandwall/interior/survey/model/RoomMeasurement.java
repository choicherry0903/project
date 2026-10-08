package com.roofandwall.interior.survey.model;

import com.roofandwall.interior.common.model.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity
@Table(name = "room_measurements")
public class RoomMeasurement extends BaseEntity {

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long surveyorId;

    @Column(nullable = false, length = 64)
    private String roomKey;

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
        String roomKey,
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
        this.roomKey = roomKey;
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

    public String getRoomKey() {
        return roomKey;
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
