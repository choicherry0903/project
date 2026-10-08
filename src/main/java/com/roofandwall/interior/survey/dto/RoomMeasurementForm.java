package com.roofandwall.interior.survey.dto;

import com.roofandwall.interior.survey.model.RoomMeasurement;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class RoomMeasurementForm {

    @NotBlank(message = "공간 이름을 입력해 주세요.")
    @Size(max = 100, message = "공간 이름은 100자 이하여야 합니다.")
    private String roomName;

    @NotNull(message = "둘레를 입력해 주세요.")
    @DecimalMin(value = "0.01", message = "둘레는 0보다 커야 합니다.")
    @Digits(integer = 6, fraction = 2, message = "둘레는 소수점 2자리 이내여야 합니다.")
    private BigDecimal perimeter;

    @NotNull(message = "높이를 입력해 주세요.")
    @DecimalMin(value = "0.01", message = "높이는 0보다 커야 합니다.")
    @Digits(integer = 6, fraction = 2, message = "높이는 소수점 2자리 이내여야 합니다.")
    private BigDecimal height;

    @NotNull(message = "개구부 면적을 입력해 주세요.")
    @DecimalMin(value = "0.00", message = "개구부 면적은 0 이상이어야 합니다.")
    @Digits(integer = 6, fraction = 2, message = "개구부 면적은 소수점 2자리 이내여야 합니다.")
    private BigDecimal openingArea = BigDecimal.ZERO;

    @NotNull(message = "천장 면적을 입력해 주세요.")
    @DecimalMin(value = "0.00", message = "천장 면적은 0 이상이어야 합니다.")
    @Digits(integer = 6, fraction = 2, message = "천장 면적은 소수점 2자리 이내여야 합니다.")
    private BigDecimal ceilingArea = BigDecimal.ZERO;

    private boolean includeCeiling;

    @Size(max = 1000, message = "표면 상태는 1000자 이하여야 합니다.")
    private String surfaceCondition;

    public static RoomMeasurementForm from(RoomMeasurement measurement) {
        var form = new RoomMeasurementForm();
        form.roomName = measurement.getRoomName();
        form.perimeter = measurement.getPerimeter();
        form.height = measurement.getHeight();
        form.openingArea = measurement.getOpeningArea();
        form.ceilingArea = measurement.getCeilingArea();
        form.includeCeiling = measurement.isIncludeCeiling();
        form.surfaceCondition = measurement.getSurfaceCondition();
        return form;
    }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public BigDecimal getPerimeter() { return perimeter; }
    public void setPerimeter(BigDecimal perimeter) { this.perimeter = perimeter; }
    public BigDecimal getHeight() { return height; }
    public void setHeight(BigDecimal height) { this.height = height; }
    public BigDecimal getOpeningArea() { return openingArea; }
    public void setOpeningArea(BigDecimal openingArea) { this.openingArea = openingArea; }
    public BigDecimal getCeilingArea() { return ceilingArea; }
    public void setCeilingArea(BigDecimal ceilingArea) { this.ceilingArea = ceilingArea; }
    public boolean isIncludeCeiling() { return includeCeiling; }
    public void setIncludeCeiling(boolean includeCeiling) { this.includeCeiling = includeCeiling; }
    public String getSurfaceCondition() { return surfaceCondition; }
    public void setSurfaceCondition(String surfaceCondition) { this.surfaceCondition = surfaceCondition; }
}
