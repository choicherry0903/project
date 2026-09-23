package com.barunsigong.interior.survey.repository;
import com.barunsigong.interior.survey.model.RoomMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface RoomMeasurementRepository extends JpaRepository<RoomMeasurement, Long> {
    Optional<RoomMeasurement> findByIdAndCompanyId(Long id, Long companyId);
}
