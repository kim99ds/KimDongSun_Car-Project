package com.carproject.car.controller;

import com.carproject.car.dto.TrimDetailDto;
import com.carproject.car.service.TrimService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trims")
public class TrimApiController {

    private final TrimService trimService;

    // 트림 선택 시 색상 / 옵션 조회용 API 컨트롤러 -> 페이지이동이 없고 JSON만 내려주는 용도임
    @GetMapping("/{trimId}")
    public TrimDetailDto getTrimDetail(@PathVariable Long trimId) {
        return trimService.getTrimDetail(trimId);
    }

}
