package com.lankatrain.seatbooking.dto;

public record SeatResponse(
        Long id,
        String seatNumber,
        String coachNumber,
        String coachType
) {
}