package com.rms.repository;

import com.rms.entity.UserNotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserNotificationPreferenceRepository extends JpaRepository<UserNotificationPreference, Long> {

    @Query("SELECT unp FROM UserNotificationPreference unp WHERE unp.user.id = :userId")
    Optional<UserNotificationPreference> findByUserId(@Param("userId") Long userId);
}
