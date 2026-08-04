package com.lankatrain.seatbooking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        String passengerName,
        Long seatId,
        String seatNumber,
        String coachNumber,
        String coachType,
        String origin,
        String destination,
        BigDecimal fare,
        LocalDateTime bookingTime
) {
}