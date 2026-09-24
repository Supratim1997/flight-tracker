package com.flighttracker.repository;

import com.flighttracker.entity.PriceAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceAccessLogRepository extends JpaRepository<PriceAccessLog, Long> {
    List<PriceAccessLog> findTop100ByOrderByAccessedAtDesc();
    List<PriceAccessLog> findTop100BySearchConfigIdOrderByAccessedAtDesc(Long configId);
    void deleteBySearchConfigId(Long configId);
}
