package com.barunsigong.interior;

import static org.assertj.core.api.Assertions.*;

import com.barunsigong.interior.estimate.service.EstimateCalculator;
import com.barunsigong.interior.schedule.service.ScheduleConflictPolicy;
import com.barunsigong.interior.survey.service.MeasurementCalculator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class BusinessPolicyTest {

    private final EstimateCalculator estimates = new EstimateCalculator();

    @Test
    void estimateMatchesDesignExample() {
        var total = estimates.calculate(
            List.of(line("5", 80000), line("4", 350000), line("1", 200000), line("1", 80000)),
            100000,
            new BigDecimal("0.10")
        );
        assertThat(total.supplyAmount()).isEqualTo(1980000);
        assertThat(total.taxAmount()).isEqualTo(198000);
        assertThat(total.totalAmount()).isEqualTo(2178000);
    }

    @Test
    void amountsRoundHalfUpAndInvalidDiscountIsBlocked() {
        assertThat(estimates.calculate(List.of(line("0.50", 1)), 0, BigDecimal.ZERO).totalAmount()).isEqualTo(1);
        assertThatThrownBy(() -> estimates.calculate(List.of(line("1", 100)), 101, BigDecimal.ZERO)).isInstanceOf(
            IllegalArgumentException.class
        );
        assertThatThrownBy(() -> estimates.calculate(List.of(line("0.001", 100)), 0, BigDecimal.ZERO)).isInstanceOf(
            IllegalArgumentException.class
        );
        assertThatThrownBy(() -> estimates.calculate(List.of(line("2", 999999999)), 0, BigDecimal.ZERO)).isInstanceOf(
            IllegalArgumentException.class
        );
    }

    @Test
    void measurementAndRollsMatchDesignExample() {
        var calculator = new MeasurementCalculator();
        assertThat(
            calculator.wallArea(new BigDecimal("18"), new BigDecimal("2.30"), new BigDecimal("8"))
        ).isEqualByComparingTo("33.40");
        assertThat(
            calculator.recommendedRolls(new BigDecimal("61.60"), new BigDecimal("0.10"), new BigDecimal("15"))
        ).isEqualTo(5);
        assertThatThrownBy(() -> calculator.wallArea(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.TEN)).isInstanceOf(
            IllegalArgumentException.class
        );
    }

    @Test
    void adjacentSchedulesAreAllowedButOverlapIsBlocked() {
        var policy = new ScheduleConflictPolicy();
        var start = Instant.parse("2026-10-05T00:00:00Z");
        var end = start.plusSeconds(3600);
        assertThat(policy.overlaps(start, end, end, end.plusSeconds(3600))).isFalse();
        assertThat(policy.overlaps(start, end, start.plusSeconds(1), end)).isTrue();
    }

    private EstimateCalculator.Line line(String quantity, long unitPrice) {
        return new EstimateCalculator.Line(new BigDecimal(quantity), unitPrice);
    }
}
