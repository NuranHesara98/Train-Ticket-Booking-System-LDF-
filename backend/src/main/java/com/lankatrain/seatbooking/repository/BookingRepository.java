package com.lankatrain.seatbooking.repository;

import com.lankatrain.seatbooking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("select case when count(b) > 0 then true else false end " +
            "from Booking b " +
            "where b.seat.id = :seatId " +
            "and b.fromStation.stationOrder < :destinationOrder " +
            "and b.toStation.stationOrder > :originOrder")
    boolean existsOverlappingBooking(@Param("seatId") Long seatId,
                                     @Param("originOrder") int originOrder,
                                     @Param("destinationOrder") int destinationOrder);

    List<Booking> findAllByOrderByBookingTimeDesc();
}
