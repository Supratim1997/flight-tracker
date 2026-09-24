package com.flighttracker.repository;

import com.flighttracker.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    
    List<PriceHistory> findBySearchConfigIdOrderByFlightDateAscPriceInrAsc(Long configId);

    void deleteBySearchConfigId(Long configId);

    @Query("SELECT ph.flightDate as flightDate, MIN(ph.priceInr) as minPrice FROM PriceHistory ph WHERE ph.searchConfig.id = :configId GROUP BY ph.flightDate ORDER BY ph.flightDate ASC")
    List<Map<String, Object>> findDailyMinimumsByConfigId(@Param("configId") Long configId);
}
