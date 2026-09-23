package com.barunsigong.interior.survey.repository;

import com.barunsigong.interior.survey.model.RoomMeasurement;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomMeasurementRepository extends JpaRepository<RoomMeasurement, Long> {
    Optional<RoomMeasurement> findByIdAndCompanyId(Long id, Long companyId);
}
