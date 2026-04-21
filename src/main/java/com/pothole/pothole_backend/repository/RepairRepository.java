package com.pothole.pothole_backend.repository;

import com.pothole.pothole_backend.model.Repair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
public interface RepairRepository extends JpaRepository<Repair, Integer> {
    List<Repair> findByAuthorityId(Integer authorityId);
    Optional<Repair> findByPotholeId(Integer potholeId);
}