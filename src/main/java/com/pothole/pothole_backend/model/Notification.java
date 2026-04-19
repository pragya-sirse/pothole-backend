package com.pothole.pothole_backend.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pothole_id", nullable = false)
    private Pothole pothole;
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "authority_id")
    private Authority authority;
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
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

    public enum Type {
        new_report, status_update, repair_complete, escalation
    }

    @PrePersist
    public void prePersist() {
        this.sentAt = LocalDateTime.now();
        if (this.isSent == null) this.isSent = false;
    }
}
