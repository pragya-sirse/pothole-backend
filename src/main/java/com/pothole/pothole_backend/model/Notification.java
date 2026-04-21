package com.pothole.pothole_backend.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pothole_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Pothole pothole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authority_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Authority authority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Type type;

    @Column(name = "sent_to", length = 200)
    private String sentTo;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "is_sent")
    private Boolean isSent;

    public enum Type { new_report, status_update, repair_complete, escalation }

    @PrePersist
    public void prePersist() {
        this.sentAt = LocalDateTime.now();
        if (this.isSent == null) this.isSent = false;
    }
}
 