package com.carproject.car.repository;

import com.carproject.car.entity.ModelLike;
import com.carproject.car.entity.ModelLikeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelLikeRepository extends JpaRepository<ModelLike, ModelLikeId> {
}
