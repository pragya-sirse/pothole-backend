package com.pothole.pothole_backend.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "zones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(name = "zone_number", nullable = false)
    private Integer zoneNumber;

    @Column(name = "zone_name", nullable = false, length = 200)
    private String zoneName;

    @Column(name = "office_address", length = 300)
    private String officeAddress;

    @Column(length = 50)
    private String phone;

    @Column(name = "csi_health_no", length = 50)
    private String csiHealthNo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}