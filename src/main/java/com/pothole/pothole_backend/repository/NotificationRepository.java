package com.pothole.pothole_backend.repository;

import com.pothole.pothole_backend.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByIsSentFalse();
    List<Notification> findByUserId(Integer userId);
}