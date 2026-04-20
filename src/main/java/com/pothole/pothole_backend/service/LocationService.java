package com.pothole.pothole_backend.service;



import com.pothole.pothole_backend.model.City;
import com.pothole.pothole_backend.model.Ward;
import com.pothole.pothole_backend.model.Zone;
import com.pothole.pothole_backend.repository.CityRepository;
import com.pothole.pothole_backend.repository.WardRepository;
import com.pothole.pothole_backend.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final CityRepository cityRepository;
    private final ZoneRepository zoneRepository;
    private final WardRepository wardRepository;

    public List<City> getAllCities() {
        try {
            return cityRepository.findAll();
        } catch (Exception e) {
            log.error("getAllCities failed: {}", e.getMessage());
            throw new RuntimeException("Failed to load cities");
        }
    }

    public City getCityById(Integer id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("City not found"));
    }

    public List<Zone> getZonesByCity(Integer cityId) {
        try {
            return zoneRepository.findByCityId(cityId);
        } catch (Exception e) {
            log.error("getZonesByCity failed: {}", e.getMessage());
            throw new RuntimeException("Failed to load zones");
        }
    }

    public List<Ward> getWardsByZone(Integer zoneId) {
        try {
            return wardRepository.findByZoneId(zoneId);
        } catch (Exception e) {
            log.error("getWardsByZone failed: {}", e.getMessage());
            throw new RuntimeException("Failed to load wards");
        }
    }

    public List<Ward> getWardsByCity(Integer cityId) {
        try {
            return wardRepository.findByCityId(cityId);
        } catch (Exception e) {
            log.error("getWardsByCity failed: {}", e.getMessage());
            throw new RuntimeException("Failed to load wards");
        }
    }
}
