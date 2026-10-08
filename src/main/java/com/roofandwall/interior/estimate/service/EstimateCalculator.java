package com.roofandwall.interior.estimate.service;

import java.math.*;
import java.util.List;
import org.springframework.stereotype.Service;

/** 화면설계서 18, 35쪽. 금액 계산 결과는 추후 견적 저장·PDF에서 공통 사용합니다. */
@Service
public class EstimateCalculator {

    public static final long MAX_AMOUNT = 999_999_999L;

    public record Line(BigDecimal quantity, long unitPrice) {}

    public record Total(long subtotal, long discount, long supplyAmount, long taxAmount, long totalAmount) {}

    public Total calculate(List<Line> lines, long discount, BigDecimal taxRate) {
        if (lines == null || lines.isEmpty() || lines.size() > 100) throw new IllegalArgumentException(
            "견적 항목은 1~100개여야 합니다."
        );
        if (
            taxRate == null || taxRate.signum() < 0 || taxRate.compareTo(BigDecimal.ONE) > 0 || taxRate.scale() > 4
        ) throw new IllegalArgumentException("세율은 0~1 사이 소수점 4자리 이내여야 합니다.");
        long subtotal = 0;
        for (var line : lines) {
            if (
                line == null ||
                line.quantity() == null ||
                line.quantity().signum() <= 0 ||
                line.quantity().scale() > 2 ||
                line.quantity().compareTo(new BigDecimal("999999.99")) > 0 ||
                line.unitPrice() < 0 ||
                line.unitPrice() > MAX_AMOUNT
            ) throw new IllegalArgumentException("수량 또는 단가 범위를 확인해 주세요.");
            long amount = line
                .quantity()
                .multiply(BigDecimal.valueOf(line.unitPrice()))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
            subtotal = Math.addExact(subtotal, amount);
            if (subtotal > MAX_AMOUNT) throw new IllegalArgumentException("견적 금액 상한을 초과했습니다.");
        }
        if (discount < 0 || discount > subtotal) throw new IllegalArgumentException(
            "할인은 항목 합계 이내여야 합니다."
        );
        long supply = subtotal - discount;
        long tax = BigDecimal.valueOf(supply).multiply(taxRate).setScale(0, RoundingMode.HALF_UP).longValueExact();
        long total = supply + tax;
        if (total > MAX_AMOUNT) throw new IllegalArgumentException("견적 금액 상한을 초과했습니다.");
        return new Total(subtotal, discount, supply, tax, total);
    }
}
