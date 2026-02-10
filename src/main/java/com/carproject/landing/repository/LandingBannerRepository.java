package com.carproject.landing.repository;

import com.carproject.landing.entity.LandingBanner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LandingBannerRepository extends JpaRepository<LandingBanner, Long> {

    // 랜딩(/) 노출용: 노출(Y) + 정렬
    List<LandingBanner> findByIsVisibleOrderBySortOrderAsc(String isVisible);

    // 관리자 목록용: 정렬
    List<LandingBanner> findAllByOrderBySortOrderAsc();
}
