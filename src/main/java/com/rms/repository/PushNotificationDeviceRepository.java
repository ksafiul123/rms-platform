package com.rms.repository;

import com.rms.entity.PushNotificationDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushNotificationDeviceRepository extends JpaRepository<PushNotificationDevice, Long> {

    Optional<PushNotificationDevice> findByDeviceToken(String deviceToken);

    List<PushNotificationDevice> findByUserIdAndIsActiveTrue(Long userId);

    List<PushNotificationDevice> findByUserId(Long userId);
}
