package com.pothole.pothole_backend.repository;

import com.pothole.pothole_backend.model.Upvote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UpvoteRepository extends JpaRepository<Upvote, Integer> {
    boolean existsByPotholeIdAndUserId(Integer potholeId, Integer userId);
    Long countByPotholeId(Integer potholeId);
}
