package com.flighttracker.repository;

import com.flighttracker.entity.SearchConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SearchConfigRepository extends JpaRepository<SearchConfig, Long> {
    List<SearchConfig> findByActive(Integer active);
}
