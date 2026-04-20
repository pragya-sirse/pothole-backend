package com.pothole.pothole_backend.repository;

import com.pothole.pothole_backend.model.Pothole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PotholeRepository extends JpaRepository<Pothole, Integer> {

    List<Pothole> findByCityId(Integer cityId);

    List<Pothole> findByZoneId(Integer zoneId);

    List<Pothole> findByStatus(Pothole.Status status);

    List<Pothole> findByCityIdAndStatus(Integer cityId, Pothole.Status status);

    @Query("SELECT p FROM Pothole p WHERE p.city.id = :cityId ORDER BY p.priorityScore DESC")
    List<Pothole> findByCityIdOrderByPriority(@Param("cityId") Integer cityId);

    @Query("SELECT p FROM Pothole p WHERE " +
            "ABS(p.latitude - :lat) < 0.005 AND ABS(p.longitude - :lng) < 0.005")
    List<Pothole> findNearby(@Param("lat") Double lat, @Param("lng") Double lng);

    @Query("SELECT p FROM Pothole p WHERE p.reportedBy.id = :userId ORDER BY p.createdAt DESC")
    List<Pothole> findByReportedById(@Param("userId") Integer userId);
}
