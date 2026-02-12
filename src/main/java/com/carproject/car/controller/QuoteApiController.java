package com.carproject.car.controller;

import com.carproject.car.dto.QuoteRequestDto;
import com.carproject.car.dto.QuoteResponseDto;
import com.carproject.car.service.QuoteService;
import com.carproject.global.security.SecurityMemberResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 견적 API 컨트롤러.
 * - @Valid로 요청 검증
 */
@RestController
@RequiredArgsConstructor
public class QuoteApiController {

    private final QuoteService quoteService;
    private final SecurityMemberResolver securityMemberResolver;

    @PostMapping("/api/quotes")
    public QuoteResponseDto submit(@Valid @RequestBody QuoteRequestDto req, Authentication authentication) {
        Long memberId = securityMemberResolver.requireMemberId(authentication);
        Long quoteId = quoteService.submitQuote(memberId, req);
        return new QuoteResponseDto(String.valueOf(quoteId));
    }
}
