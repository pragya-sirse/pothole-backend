package com.pothole.pothole_backend.repository;

import com.pothole.pothole_backend.model.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Integer> {
    List<Zone> findByCityId(Integer cityId);
}
