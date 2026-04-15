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
        return cityRepository.findAll();
    }

    public City getCityById(Integer id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("City not found"));
    }

    public List<Zone> getZonesByCity(Integer cityId) {
        return zoneRepository.findByCityId(cityId);
    }

    public List<Ward> getWardsByZone(Integer zoneId) {
        return wardRepository.findByZoneId(zoneId);
    }

    public List<Ward> getWardsByCity(Integer cityId) {
        return wardRepository.findByCityId(cityId);
    }

    // Find which zone a GPS coordinate belongs to
    // Simple approach: find nearest zone based on ward
    public Zone findZoneByCoordinates(Integer cityId, Double lat, Double lng) {
        // Default: return first zone of city (in real app use GIS polygon check)
        List<Zone> zones = zoneRepository.findByCityId(cityId);
        if (zones.isEmpty()) throw new RuntimeException("No zones found for city");
        return zones.get(0);
    }
}
