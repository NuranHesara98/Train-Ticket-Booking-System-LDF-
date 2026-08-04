package com.lankatrain.seatbooking.repository;

import com.lankatrain.seatbooking.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface StationRepository extends JpaRepository<Station, Long> {
    Optional<Station> findByName(String name);

    List<Station> findAllByOrderByStationOrderAsc();
}
