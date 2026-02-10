package com.carproject.car.service;

import com.carproject.car.dto.CarModelDetailDto;
import com.carproject.car.dto.CarTrimDto;
import com.carproject.car.dto.CarVariantDto;
import com.carproject.car.entity.CarModel;
import com.carproject.car.repository.CarModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CarDetailService {

    private final CarModelRepository carModelRepository;
    // 차량 상세목록에 표시되는 정보들
    @Transactional
    public CarModelDetailDto getCarDetail(Long modelId) {
        // 조회수는 상세 진입 시점에 서버에서 증가
        carModelRepository.incrementViewCount(modelId);

        CarModel model = carModelRepository.findByIdWithVariants(modelId)
                .orElseThrow(() -> new IllegalArgumentException("차량 없음"));
        return new CarModelDetailDto(
                model.getModelId(),
                model.getModelName(),
                model.getSegment(),
                model.getBrand().getBrandName(),
                model.getViewCount(),
                model.getLikeCount(),
                model.getVariants().stream()
                        .map(v -> new CarVariantDto(
                                v.getVariantId(),
                                v.getEngineType(),
                                v.getEngineName(),
                                v.getTrims().stream()
                                        .map(t -> new CarTrimDto(
                                                t.getTrimId(),
                                                t.getTrimName(),
                                                t.getBasePrice(),
                                                t.getDescription()
                                        ))
                                        .toList()
                        ))
                        .toList()
        );
    }
}
