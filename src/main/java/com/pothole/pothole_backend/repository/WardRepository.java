package com.pothole.pothole_backend.repository;

import com.pothole.pothole_backend.model.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WardRepository extends JpaRepository<Ward, Integer> {
    List<Ward> findByZoneId(Integer zoneId);
    List<Ward> findByCityId(Integer cityId);
}
