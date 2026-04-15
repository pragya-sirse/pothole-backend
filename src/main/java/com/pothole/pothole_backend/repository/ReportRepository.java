package com.pothole.pothole_backend.repository;

import com.pothole.pothole_backend.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Integer> {
    List<Report> findByPotholeId(Integer potholeId);
    List<Report> findByUserId(Integer userId);
    boolean existsByPotholeIdAndUserId(Integer potholeId, Integer userId);
}
