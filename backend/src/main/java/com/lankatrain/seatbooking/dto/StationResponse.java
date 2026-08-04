package com.lankatrain.seatbooking.dto;

public record StationResponse(
        Long id,
        String name,
        int stationOrder
) {
}