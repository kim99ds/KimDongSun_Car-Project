package com.carproject.car.repository;

import com.carproject.car.entity.CarVariant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarVariantRepository extends JpaRepository<CarVariant, Long> {
}
