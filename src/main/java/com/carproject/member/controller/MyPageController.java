package com.carproject.member.controller;

import com.carproject.member.entity.Member;
import com.carproject.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MyPageController {

    private final MemberRepository memberRepository;

    @GetMapping("/mypage")
    public String mypage(Authentication authentication, Model model) {

        // 로그인한 사용자 아이디(너는 loginId를 usernameParameter로 쓰고 있음)
        String loginId = authentication.getName();

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalStateException("로그인 사용자 정보를 DB에서 찾을 수 없습니다. loginId=" + loginId));

        model.addAttribute("member", member);

        return "member/mypage";
    }
}
