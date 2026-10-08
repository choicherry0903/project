package com.roofandwall.interior.notification.repository;

import com.roofandwall.interior.notification.model.Notification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByIdAndCompanyId(Long id, Long companyId);
}
