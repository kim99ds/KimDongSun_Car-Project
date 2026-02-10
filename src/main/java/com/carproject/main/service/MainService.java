package com.carproject.main.service;

import com.carproject.car.repository.CarModelRepository;
import com.carproject.car.repository.BrandRepository;
import com.carproject.main.dto.BestCarCardDto;
import com.carproject.main.dto.BrandPopularDto;
import com.carproject.main.dto.BrandPopularCarDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainService {

    private final CarModelRepository carModelRepository;
    private final BrandRepository brandRepository;

    public List<BestCarCardDto> getBestTop6(Pageable pageable) {
        List<BestCarCardDto> list = carModelRepository.findBestTop6(PageRequest.of(0, 6));

        // 혹시 이미지가 null이면 기본 이미지로 치환하고 싶으면 여기서 처리 가능
        // (BestCarCardDto가 immutable이니 필요하면 DTO를 새로 만들어 반환)
        return list;
    }

    /**
     * 메인 - "브랜드 별 인기"
     * 1) BRAND에서 랜덤으로 n개 뽑고
     * 2) 각 브랜드에서 likeCount 많은 순으로 차량 4개 + exterior 이미지 1장
     */
    public List<BrandPopularDto> getBrandPopular(int brandCount, int carsPerBrand) {
        var brands = brandRepository.findRandomBrands(brandCount);

        return brands.stream()
                .map(b -> {
                    List<BrandPopularCarDto> cars =
                            carModelRepository.findTopLikedByBrand(b.getBrandId(), PageRequest.of(0, carsPerBrand));

                    return new BrandPopularDto(
                            b.getBrandId(),
                            b.getBrandName(),
                            BRAND_SUBTITLE.getOrDefault(b.getBrandName(), "인기 브랜드"),
                            cars
                    );
                })
                .toList();
    }

    // 기존 하드코딩 문구 유지가 필요하면 여기에서 매핑 (DB 컬럼으로 빼고 싶으면 BRAND에 컬럼 추가가 정석)
    private static final Map<String, String> BRAND_SUBTITLE = Map.ofEntries(
            Map.entry("기아", "움직임의 본질"),
            Map.entry("현대", "새로운 이동의 기준"),
            Map.entry("Audi", "순수한 에너지 체험"),
            Map.entry("Mercedes-Benz", "최고의 기술력"),
            Map.entry("Ferrari", "레이싱의 감성"),
            Map.entry("Genesis", "프리미엄의 정수")
    );

}