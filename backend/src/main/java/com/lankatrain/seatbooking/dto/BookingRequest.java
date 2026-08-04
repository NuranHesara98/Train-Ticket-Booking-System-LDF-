package com.lankatrain.seatbooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookingRequest(
        @NotBlank String passengerName,
        @NotBlank String origin,
        @NotBlank String destination,
        @NotNull Long seatId
) {
}