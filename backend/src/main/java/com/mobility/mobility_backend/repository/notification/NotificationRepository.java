package com.mobility.mobility_backend.repository.notification;

import com.mobility.mobility_backend.dto.socket.NotificationCategory;
import com.mobility.mobility_backend.dto.socket.NotificationMessage;
import com.mobility.mobility_backend.dto.socket.NotificationSeverity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationMessage, String> {

    @Query("SELECT n FROM NotificationMessage n WHERE n.recipient = :userId AND n.read = false ORDER BY n.createdAt DESC")
    List<NotificationMessage> findUnreadByUserId(@Param("userId") String userId);

    @Query("SELECT n FROM NotificationMessage n WHERE n.recipient = :userId ORDER BY n.createdAt DESC")
    List<NotificationMessage> findByUserId(@Param("userId") String userId);

    @Query("SELECT n FROM NotificationMessage n WHERE n.recipient = :userId AND n.category = :category ORDER BY n.createdAt DESC")
    List<NotificationMessage> findByUserIdAndCategory(@Param("userId") String userId,
                                                     @Param("category") NotificationCategory category);

    @Query("SELECT n FROM NotificationMessage n WHERE n.recipient = :userId AND n.severity = :severity ORDER BY n.createdAt DESC")
    List<NotificationMessage> findByUserIdAndSeverity(@Param("userId") String userId,
                                                     @Param("severity") NotificationSeverity severity);

    @Query("SELECT n FROM NotificationMessage n WHERE n.recipient = :userId AND n.category = :category AND n.severity = :severity ORDER BY n.createdAt DESC")
    List<NotificationMessage> findByUserIdAndCategoryAndSeverity(@Param("userId") String userId,
                                                                @Param("category") NotificationCategory category,
                                                                @Param("severity") NotificationSeverity severity);

    @Query("SELECT COUNT(n) FROM NotificationMessage n WHERE n.recipient = :userId AND n.read = false")
    long countUnreadByUserId(@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query("UPDATE NotificationMessage n SET n.read = true WHERE n.id = :notificationId AND n.recipient = :userId")
    void markAsRead(@Param("notificationId") String notificationId, @Param("userId") String userId);

    @Modifying
    @Transactional
    @Query("UPDATE NotificationMessage n SET n.read = true WHERE n.recipient = :userId AND n.read = false")
    void markAllAsRead(@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM NotificationMessage n WHERE n.expiresAt < CURRENT_TIMESTAMP")
    void deleteExpiredNotifications();
}