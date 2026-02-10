package com.carproject.global.health;

import com.carproject.car.repository.BrandRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DbCheckController {

    private final BrandRepository brandRepository;

    public DbCheckController(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    @GetMapping("/db-check")
    public String dbCheck() {
        long cnt = brandRepository.count();
        return "db ok, brand count=" + cnt;
    }
}
