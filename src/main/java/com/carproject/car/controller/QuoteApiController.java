package com.carproject.car.controller;

import com.carproject.car.dto.QuoteRequestDto;
import com.carproject.car.dto.QuoteResponseDto;
import com.carproject.car.service.QuoteService;
import com.carproject.global.security.SecurityMemberResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class QuoteApiController {

    private final QuoteService quoteService;
    private final SecurityMemberResolver securityMemberResolver;

    @PostMapping("/api/quotes")
    public QuoteResponseDto submit(@RequestBody QuoteRequestDto req, Authentication authentication) {

        // ✅ 로그인 사용자 memberId를 SecurityMemberResolver로 구함
        Long memberId = securityMemberResolver.requireMemberId(authentication);

        Long quoteId = quoteService.submitQuote(memberId, req);
        return new QuoteResponseDto(String.valueOf(quoteId));
    }
}
