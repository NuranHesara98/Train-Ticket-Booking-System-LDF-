package com.lankatrain.seatbooking.controller;

import com.lankatrain.seatbooking.dto.StationResponse;
import com.lankatrain.seatbooking.repository.StationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
public class StationController {
    private final StationRepository stationRepository;

    public StationController(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @GetMapping
    public ResponseEntity<List<StationResponse>> listStations() {
        List<StationResponse> stations = stationRepository.findAllByOrderByStationOrderAsc()
                .stream()
                .map(station -> new StationResponse(station.getId(), station.getName(), station.getStationOrder()))
                .toList();
        return ResponseEntity.ok(stations);
    }
}