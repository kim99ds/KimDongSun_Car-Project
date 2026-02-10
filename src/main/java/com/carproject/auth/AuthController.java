package com.carproject.auth;

import com.carproject.auth.dto.SignupForm;
import com.carproject.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;

    @GetMapping("/auth/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/auth/signup")
    public String signupPage(@ModelAttribute("form") SignupForm form) {
        return "auth/signup";
    }

    @PostMapping("/auth/signup")
    public String signup(@Valid @ModelAttribute("form") SignupForm form,
                         BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }

        try {
            memberService.signup(
                    form.getLoginId(),
                    form.getPassword(),
                    form.getName(),
                    form.getEmail(),
                    form.getBirthDate()
            );
        } catch (IllegalArgumentException e) {
            bindingResult.reject("signupFail", e.getMessage());
            return "auth/signup";
        }

        // ✅ 회원가입 성공 → 메인으로 이동 + 모달 파라미터
        return "redirect:/main?auth=signup_success";
    }
}
