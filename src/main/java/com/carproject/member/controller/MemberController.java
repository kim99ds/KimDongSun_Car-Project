package com.carproject.member.controller;

import com.carproject.member.entity.Member;
import com.carproject.member.repository.MemberRepository;
import com.carproject.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;
    private final MemberService memberService;

    @GetMapping("/detail")
    public String detail(Model model, Authentication authentication) {
        String loginId = authentication.getName(); // Security username = loginId

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("회원 정보 없음: " + loginId));

        model.addAttribute("member", member);
        return "member/detail";
    }

    /**
     * 일반 유저: 내 프로필(name/email) 수정
     * - 대상은 Authentication.getName() (loginId) 기반으로만 수정
     */
    @PostMapping("/update")
    public String updateProfile(@RequestParam String name,
                                @RequestParam String email,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {

        String loginId = authentication.getName();

        try {
            memberService.updateProfile(loginId, name, email);
            redirectAttributes.addFlashAttribute("successMessage", "회원 정보가 저장되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/member/detail";
    }
}
