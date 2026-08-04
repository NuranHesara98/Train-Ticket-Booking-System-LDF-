package com.lankatrain.seatbooking.service;

import com.lankatrain.seatbooking.dto.BookingRequest;
import com.lankatrain.seatbooking.dto.BookingResponse;
import com.lankatrain.seatbooking.dto.SeatResponse;
import com.lankatrain.seatbooking.dto.StationResponse;
import com.lankatrain.seatbooking.entity.Booking;
import com.lankatrain.seatbooking.entity.CoachType;
import com.lankatrain.seatbooking.entity.Seat;
import com.lankatrain.seatbooking.entity.Station;
import com.lankatrain.seatbooking.repository.BookingRepository;
import com.lankatrain.seatbooking.repository.SeatRepository;
import com.lankatrain.seatbooking.repository.StationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final StationRepository stationRepository;

    @Value("${app.booking.base-fare-per-station:150}")
    private BigDecimal baseFarePerStation;

    public BookingService(BookingRepository bookingRepository,
                          SeatRepository seatRepository,
                          StationRepository stationRepository) {
        this.bookingRepository = bookingRepository;
        this.seatRepository = seatRepository;
        this.stationRepository = stationRepository;
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        Station origin = stationRepository.findByName(request.origin())
                .orElseThrow(() -> new IllegalArgumentException("Origin station not found"));
        Station destination = stationRepository.findByName(request.destination())
                .orElseThrow(() -> new IllegalArgumentException("Destination station not found"));
        Seat seat = seatRepository.findByIdForUpdate(request.seatId())
                .orElseThrow(() -> new IllegalArgumentException("Seat not found"));

        if (seat.getCoach().getType() != CoachType.RESERVED) {
            throw new IllegalArgumentException("Only reserved seats can be booked");
        }

        int originOrder = origin.getStationOrder();
        int destinationOrder = destination.getStationOrder();

        if (originOrder >= destinationOrder) {
            throw new IllegalArgumentException("Origin must be before destination");
        }

        boolean occupied = bookingRepository.existsOverlappingBooking(seat.getId(), originOrder, destinationOrder);
        if (occupied) {
            throw new IllegalStateException("Selected seat is already booked for an overlapping segment");
        }

        Booking booking = new Booking();
        booking.setPassengerName(request.passengerName());
        booking.setFromStation(origin);
        booking.setToStation(destination);
        booking.setSeat(seat);
        booking.setFare(calculateFare(originOrder, destinationOrder));
        Booking savedBooking = bookingRepository.save(booking);
        return mapToResponse(savedBooking);
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> findAvailableSeats(String originName, String destinationName) {
        Station origin = stationRepository.findByName(originName)
                .orElseThrow(() -> new IllegalArgumentException("Origin station not found"));
        Station destination = stationRepository.findByName(destinationName)
                .orElseThrow(() -> new IllegalArgumentException("Destination station not found"));
        return seatRepository.findAvailableSeatsForSegment(origin.getStationOrder(), destination.getStationOrder())
                .stream()
                .map(this::mapSeat)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StationResponse> listStations() {
        return stationRepository.findAllByOrderByStationOrderAsc()
                .stream()
                .map(station -> new StationResponse(station.getId(), station.getName(), station.getStationOrder()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listBookings() {
        return bookingRepository.findAllByOrderByBookingTimeDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private BigDecimal calculateFare(int originOrder, int destinationOrder) {
        return baseFarePerStation.multiply(BigDecimal.valueOf(destinationOrder - originOrder));
    }

    private BookingResponse mapToResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getPassengerName(),
                booking.getSeat().getId(),
                booking.getSeat().getSeatNumber(),
                booking.getSeat().getCoach().getCoachNumber(),
                booking.getSeat().getCoach().getType().name(),
                booking.getFromStation().getName(),
                booking.getToStation().getName(),
                booking.getFare(),
                booking.getBookingTime()
        );
    }

    private SeatResponse mapSeat(Seat seat) {
        return new SeatResponse(
                seat.getId(),
                seat.getSeatNumber(),
                seat.getCoach().getCoachNumber(),
                seat.getCoach().getType().name()
        );
    }
}
