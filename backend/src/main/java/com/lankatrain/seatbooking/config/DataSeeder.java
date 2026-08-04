package com.lankatrain.seatbooking.config;

import com.lankatrain.seatbooking.entity.Coach;
import com.lankatrain.seatbooking.entity.CoachType;
import com.lankatrain.seatbooking.entity.Seat;
import com.lankatrain.seatbooking.entity.Station;
import com.lankatrain.seatbooking.repository.CoachRepository;
import com.lankatrain.seatbooking.repository.SeatRepository;
import com.lankatrain.seatbooking.repository.StationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DataSeeder {

    @Value("${app.booking.route.stations}")
    private String stationNamesCsv;

    @Value("${app.booking.route.reserved-coach-count}")
    private int reservedCoachCount;

    @Value("${app.booking.route.total-coach-count}")
    private int totalCoachCount;

    @Value("${app.booking.route.seats-per-coach}")
    private int seatsPerCoach;

    @Bean
    public CommandLineRunner seedData(StationRepository stationRepository,
                                      CoachRepository coachRepository,
                                      SeatRepository seatRepository) {
        return args -> {
            List<String> stationNames = Arrays.stream(stationNamesCsv.split(","))
                    .map(name -> name.trim())
                    .filter(name -> !name.isBlank())
                    .toList();

            if (stationRepository.count() == 0) {
                for (int i = 0; i < stationNames.size(); i++) {
                    Station station = new Station();
                    station.setName(stationNames.get(i));
                    station.setStationOrder(i + 1);
                    stationRepository.save(station);
                }
            }

            if (coachRepository.count() == 0) {
                for (int coachIndex = 1; coachIndex <= totalCoachCount; coachIndex++) {
                    Coach coach = new Coach();
                    coach.setCoachNumber(coachIndex <= reservedCoachCount
                            ? "R" + coachIndex
                            : "U" + (coachIndex - reservedCoachCount));
                    coach.setType(coachIndex <= reservedCoachCount ? CoachType.RESERVED : CoachType.UNRESERVED);
                    coachRepository.save(coach);
                }
            }

            if (seatRepository.count() == 0) {
                for (Coach coach : coachRepository.findAll()) {
                    for (int seatNumber = 1; seatNumber <= seatsPerCoach; seatNumber++) {
                        Seat seat = new Seat();
                        seat.setCoach(coach);
                        seat.setSeatNumber(String.format("%s-%02d", coach.getCoachNumber(), seatNumber));
                        seatRepository.save(seat);
                    }
                }
            }
        };
    }
}
