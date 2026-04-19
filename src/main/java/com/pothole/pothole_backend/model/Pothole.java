package com.pothole.pothole_backend.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Potholes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pothole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id")
    private Ward ward;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Severity severity;

    @Column(name = "severity_score")
    private Float severityScore;

    @Column(name = "confidence_score")
    private Float confidenceScore;

    @Column(name = "bbox_width")
    private Integer bboxWidth;

    @Column(name = "bbox_height")
    private Integer bboxHeight;

    @Enumerated(EnumType.STRING)
    @Column(name = "detected_by", length = 10)
    private DetectedBy detectedBy;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private Status status;

    @Column(name = "priority_score")
    private Integer priorityScore;

    @Column(name = "upvote_count")
    private Integer upvoteCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "road_type", length = 15)
    private RoadType roadType;
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by")
    private User reportedBy;
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private Authority assignedTo;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Severity { low, medium, high }
    public enum Status { pending, in_progress, completed, rejected }
    public enum DetectedBy { citizen, auto }
    public enum RoadType { highway, main_road, internal, unknown }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = Status.pending;
        if (this.upvoteCount == null) this.upvoteCount = 0;
        if (this.priorityScore == null) this.priorityScore = 0;
        if (this.detectedBy == null) this.detectedBy = DetectedBy.citizen;
        if (this.roadType == null) this.roadType = RoadType.unknown;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
