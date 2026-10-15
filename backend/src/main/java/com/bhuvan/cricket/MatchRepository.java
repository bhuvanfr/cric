package com.bhuvan.cricket;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {
    boolean existsBySourceId(String sourceId);
}
