package com.flighttracker.repository;

import com.flighttracker.entity.AlertLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertLogRepository extends JpaRepository<AlertLog, Long> {

    @Query("SELECT a FROM AlertLog a WHERE a.searchConfig.id = :configId AND a.sentAt > :cutoffTime AND a.message LIKE %:datePattern%")
    List<AlertLog> findRecentAlertsForDate(@Param("configId") Long configId, @Param("cutoffTime") LocalDateTime cutoffTime, @Param("datePattern") String datePattern);

    void deleteBySearchConfigId(Long configId);
}
