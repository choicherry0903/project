package com.barunsigong.interior.survey.service;

import java.math.*;
import org.springframework.stereotype.Service;

/** 화면설계서 16쪽. 실제 상품별 롤 기준과 손실률을 전달해야 합니다. */
@Service
public class MeasurementCalculator {

    public BigDecimal wallArea(BigDecimal perimeter, BigDecimal height, BigDecimal openingArea) {
        validate(perimeter, true);
        validate(height, true);
        validate(openingArea, false);
        BigDecimal area = perimeter.multiply(height).subtract(openingArea);
        if (area.signum() < 0) throw new IllegalArgumentException("개구부 면적은 전체 벽면보다 클 수 없습니다.");
        return area.setScale(2, RoundingMode.HALF_UP);
    }

    public long recommendedRolls(BigDecimal area, BigDecimal wasteRate, BigDecimal coveragePerRoll) {
        validate(area, false);
        validate(coveragePerRoll, true);
        if (
            wasteRate == null || wasteRate.signum() < 0 || wasteRate.compareTo(BigDecimal.ONE) > 0
        ) throw new IllegalArgumentException("손실률 범위를 확인해 주세요.");
        return area
            .multiply(BigDecimal.ONE.add(wasteRate))
            .divide(coveragePerRoll, 0, RoundingMode.CEILING)
            .longValueExact();
    }

    private void validate(BigDecimal value, boolean positive) {
        if (
            value == null ||
            value.signum() < 0 ||
            (positive && value.signum() == 0) ||
            value.scale() > 2 ||
            value.compareTo(new BigDecimal("999999.99")) > 0
        ) throw new IllegalArgumentException("치수는 허용 범위 내 소수점 2자리 이내여야 합니다.");
    }
}
