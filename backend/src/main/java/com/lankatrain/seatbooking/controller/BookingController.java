package com.lankatrain.seatbooking.controller;

import com.lankatrain.seatbooking.dto.BookingRequest;
import com.lankatrain.seatbooking.dto.BookingResponse;
import com.lankatrain.seatbooking.dto.ResetBookingsResponse;
import com.lankatrain.seatbooking.dto.SeatResponse;
import com.lankatrain.seatbooking.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@Validated
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> listBookings() {
        return ResponseEntity.ok(bookingService.listBookings());
    }

    @GetMapping("/available")
    public ResponseEntity<List<SeatResponse>> findAvailableSeats(@RequestParam String origin, @RequestParam String destination) {
        return ResponseEntity.ok(bookingService.findAvailableSeats(origin, destination));
    }

    @DeleteMapping
    public ResponseEntity<ResetBookingsResponse> resetAllBookings() {
        return ResponseEntity.ok(bookingService.resetAllBookings());
    }
}
