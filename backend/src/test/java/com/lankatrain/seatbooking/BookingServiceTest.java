package com.lankatrain.seatbooking;

import com.lankatrain.seatbooking.entity.Coach;
import com.lankatrain.seatbooking.entity.CoachType;
import com.lankatrain.seatbooking.entity.Seat;
import com.lankatrain.seatbooking.entity.Station;
import com.lankatrain.seatbooking.dto.SeatResponse;
import com.lankatrain.seatbooking.repository.CoachRepository;
import com.lankatrain.seatbooking.repository.SeatRepository;
import com.lankatrain.seatbooking.repository.StationRepository;
import com.lankatrain.seatbooking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(BookingService.class)
class BookingServiceTest {

    @Autowired private BookingService bookingService;
    @Autowired private StationRepository stationRepository;
    @Autowired private CoachRepository coachRepository;
    @Autowired private SeatRepository seatRepository;

    @Test
    void shouldAllowAdjacentSegmentsOnTheSameSeat() {
        Station origin = new Station();
        origin.setName("Colombo Fort");
        origin.setStationOrder(1);
        stationRepository.save(origin);

        Station middle = new Station();
        middle.setName("Kandy");
        middle.setStationOrder(2);
        stationRepository.save(middle);

        Station destination = new Station();
        destination.setName("Badulla");
        destination.setStationOrder(3);
        stationRepository.save(destination);

        Coach coach = new Coach();
        coach.setCoachNumber("R1");
        coach.setType(CoachType.RESERVED);
        coach = coachRepository.save(coach);

        Seat seat = new Seat();
        seat.setCoach(coach);
        seat.setSeatNumber("R1-01");
        Seat savedSeat = seatRepository.save(seat);

        bookingService.createBooking(new com.lankatrain.seatbooking.dto.BookingRequest("Alice", "Colombo Fort", "Kandy", savedSeat.getId()));

        var availableSeats = bookingService.findAvailableSeats("Kandy", "Badulla");

        boolean foundSeat = false;
        for (SeatResponse seatResponse : availableSeats) {
            if (seatResponse.id().equals(savedSeat.getId())) {
                foundSeat = true;
                break;
            }
        }

        assertThat(foundSeat).isTrue();
    }

    @Test
    void shouldRejectOverlappingSegmentsOnTheSameSeat() {
        Station origin = new Station();
        origin.setName("Colombo Fort");
        origin.setStationOrder(1);
        stationRepository.save(origin);

        Station middle = new Station();
        middle.setName("Kandy");
        middle.setStationOrder(2);
        stationRepository.save(middle);

        Station destination = new Station();
        destination.setName("Badulla");
        destination.setStationOrder(3);
        stationRepository.save(destination);

        Coach coach = new Coach();
        coach.setCoachNumber("R1");
        coach.setType(CoachType.RESERVED);
        coach = coachRepository.save(coach);

        Seat seat = new Seat();
        seat.setCoach(coach);
        seat.setSeatNumber("R1-01");
        Seat savedSeat = seatRepository.save(seat);

        bookingService.createBooking(new com.lankatrain.seatbooking.dto.BookingRequest("Alice", "Colombo Fort", "Kandy", savedSeat.getId()));

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
            bookingService.createBooking(new com.lankatrain.seatbooking.dto.BookingRequest("Bob", "Colombo Fort", "Badulla", savedSeat.getId()))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldResetAllBookings() {
        Station origin = new Station();
        origin.setName("Colombo Fort");
        origin.setStationOrder(1);
        stationRepository.save(origin);

        Station destination = new Station();
        destination.setName("Badulla");
        destination.setStationOrder(2);
        stationRepository.save(destination);

        Coach coach = new Coach();
        coach.setCoachNumber("R1");
        coach.setType(CoachType.RESERVED);
        coach = coachRepository.save(coach);

        Seat seat = new Seat();
        seat.setCoach(coach);
        seat.setSeatNumber("R1-01");
        Seat savedSeat = seatRepository.save(seat);

        bookingService.createBooking(new com.lankatrain.seatbooking.dto.BookingRequest("Alice", "Colombo Fort", "Badulla", savedSeat.getId()));

        var resetResponse = bookingService.resetAllBookings();

        assertThat(resetResponse.deletedCount()).isEqualTo(1);
        assertThat(bookingService.listBookings()).isEmpty();
        assertThat(bookingService.findAvailableSeats("Colombo Fort", "Badulla")).hasSize(1);
    }
}
