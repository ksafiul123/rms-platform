package com.rms.repository;

// NotificationTemplateRepository.java
//package com.rms.repository;

import com.rms.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    List<NotificationTemplate> findByCodeAndIsActiveTrue(String code);

    Optional<NotificationTemplate> findByCodeAndChannelAndIsActiveTrue(
            String code, NotificationTemplate.NotificationChannel channel);

    List<NotificationTemplate> findByChannelAndIsActiveTrue(
            NotificationTemplate.NotificationChannel channel);

    List<NotificationTemplate> findByTypeAndIsActiveTrue(
            NotificationTemplate.NotificationType type);
}