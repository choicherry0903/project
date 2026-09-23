package com.barunsigong.interior.schedule.service;

import java.time.Instant;
import org.springframework.stereotype.Service;

/** 경계 시각이 같은 일정은 허용합니다. 실제 배정 저장 시에는 작업자 잠금도 필요합니다. */
@Service
public class ScheduleConflictPolicy {

    public boolean overlaps(Instant existingStart, Instant existingEnd, Instant newStart, Instant newEnd) {
        if (
            existingStart == null ||
            existingEnd == null ||
            newStart == null ||
            newEnd == null ||
            !existingEnd.isAfter(existingStart) ||
            !newEnd.isAfter(newStart)
        ) throw new IllegalArgumentException("종료 시각은 시작보다 늦어야 합니다.");
        return existingStart.isBefore(newEnd) && existingEnd.isAfter(newStart);
    }
}
