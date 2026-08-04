package com.lankatrain.seatbooking.repository;

import com.lankatrain.seatbooking.entity.CoachType;
import com.lankatrain.seatbooking.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s join fetch s.coach where s.id = :id")
    Optional<Seat> findByIdForUpdate(@Param("id") Long id);

    @Query("select s from Seat s join fetch s.coach c where c.type = com.lankatrain.seatbooking.entity.CoachType.RESERVED order by c.coachNumber, s.seatNumber")
    List<Seat> findReservedSeats();

    @Query("select s " +
            "from Seat s " +
            "join fetch s.coach c " +
            "where c.type = com.lankatrain.seatbooking.entity.CoachType.RESERVED " +
            "and not exists ( " +
            "    select 1 " +
            "    from Booking b " +
            "    where b.seat = s " +
            "    and b.fromStation.stationOrder < :destinationOrder " +
            "    and b.toStation.stationOrder > :originOrder " +
            ") " +
            "order by c.coachNumber, s.seatNumber")
    List<Seat> findAvailableSeatsForSegment(@Param("originOrder") int originOrder,
                                            @Param("destinationOrder") int destinationOrder);
}
