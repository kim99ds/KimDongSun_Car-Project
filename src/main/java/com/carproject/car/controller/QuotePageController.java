package com.carproject.car.controller;

import com.carproject.car.dto.QuoteViewDto;
import com.carproject.car.service.QuoteService;
import com.carproject.global.security.SecurityMemberResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class QuotePageController {

    private final QuoteService quoteService;
    private final SecurityMemberResolver securityMemberResolver;

    @GetMapping("/quotes/{quoteId}")
    public String view(@PathVariable Long quoteId, Model model, Authentication authentication) {

        Long memberId = securityMemberResolver.requireMemberId(authentication);

        QuoteViewDto quote = quoteService.getQuoteView(memberId, quoteId);
        model.addAttribute("quote", quote);

        return "car/quote/quote-view";
    }

    /**
     * 내 견적서 목록
     */
    @GetMapping("/quotes")
    public String myQuotes(Model model, Authentication authentication) {
        Long memberId = securityMemberResolver.requireMemberId(authentication);
        model.addAttribute("quotes", quoteService.getMyQuotes(memberId));
        return "car/quote/quote-list";
    }


@PostMapping("/quotes/{quoteId}/delete")
public String delete(@PathVariable Long quoteId, Authentication authentication) {
    Long memberId = securityMemberResolver.requireMemberId(authentication);
    quoteService.deleteQuote(memberId, quoteId);
    return "redirect:/quotes";
}
}
