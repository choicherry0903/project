package com.barunsigong.interior.survey.repository;

import com.barunsigong.interior.survey.model.RoomMeasurement;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

public interface RoomMeasurementRepository extends JpaRepository<RoomMeasurement, Long> {
    Optional<RoomMeasurement> findByIdAndCompanyId(Long id, Long companyId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RoomMeasurement> findByIdAndCompanyIdAndProjectId(Long id, Long companyId, Long projectId);

    Optional<RoomMeasurement> findTopByCompanyIdAndProjectIdAndRoomKeyOrderByRevisionDesc(
        Long companyId, Long projectId, String roomKey
    );

    List<RoomMeasurement> findByCompanyIdAndProjectIdAndRoomKeyOrderByRevisionDesc(
        Long companyId, Long projectId, String roomKey
    );

    @Query("""
        select m from RoomMeasurement m
        where m.companyId = :companyId and m.projectId = :projectId
          and m.revision = (
            select max(history.revision) from RoomMeasurement history
            where history.companyId = m.companyId
              and history.projectId = m.projectId
              and history.roomKey = m.roomKey
          )
        order by m.roomName asc
        """)
    List<RoomMeasurement> findLatestByProject(Long companyId, Long projectId);
}
