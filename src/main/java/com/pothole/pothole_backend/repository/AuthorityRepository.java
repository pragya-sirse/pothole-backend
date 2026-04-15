package com.pothole.pothole_backend.repository;

import com.pothole.pothole_backend.model.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Integer> {
    List<Authority> findByZoneId(Integer zoneId);
    Optional<Authority> findFirstByZoneIdAndIsActiveTrue(Integer zoneId);
}
