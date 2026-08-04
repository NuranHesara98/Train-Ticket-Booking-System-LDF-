package com.lankatrain.seatbooking.repository;

import com.lankatrain.seatbooking.entity.Coach;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoachRepository extends JpaRepository<Coach, Long> {
}