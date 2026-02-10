package com.carproject.car.repository;

import com.carproject.car.entity.CarColor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarColorRepository extends JpaRepository<CarColor, Long> {
}
