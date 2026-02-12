package com.carproject.member.controller;

import com.carproject.member.entity.Member;
import com.carproject.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;


@Controller
@RequiredArgsConstructor
public class MyPageUpdateController {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    @PostMapping("/mypage/update")
    @Transactional
    public String updateMyInfo(Authentication authentication,
                               @RequestParam String name,
                               @RequestParam String email,
                               RedirectAttributes ra) {

        String loginId = authentication.getName();
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalStateException("로그인 사용자 정보를 DB에서 찾을 수 없습니다. loginId=" + loginId));

        String n = name == null ? "" : name.trim();
        String e = email == null ? "" : email.trim();

        if (n.isBlank()) {
            ra.addFlashAttribute("errorMessage", "이름을 입력하세요.");
            return "redirect:/mypage";
        }
        if (e.isBlank()) {
            ra.addFlashAttribute("errorMessage", "이메일을 입력하세요.");
            return "redirect:/mypage";
        }

        // 이메일 중복 체크 (본인 제외)
        memberRepository.findByEmail(e)
                .filter(other -> !other.getMemberId().equals(member.getMemberId()))
                .ifPresent(other -> { throw new IllegalArgumentException("이미 사용 중인 이메일입니다."); });

        member.setName(n);
        member.setEmail(e);

        ra.addFlashAttribute("successMessage", "회원 정보가 수정되었습니다.");
        return "redirect:/mypage";
    }

    @PostMapping("/mypage/updatePw")
    @Transactional
    public String updatePassword(Authentication authentication,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes ra) {

        String loginId = authentication.getName();
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalStateException("로그인 사용자 정보를 DB에서 찾을 수 없습니다. loginId=" + loginId));

        String cur = currentPassword == null ? "" : currentPassword.trim();
        String nw  = newPassword == null ? "" : newPassword.trim();
        String cf  = confirmPassword == null ? "" : confirmPassword.trim();

        if (cur.isBlank() || nw.isBlank() || cf.isBlank()) {
            ra.addFlashAttribute("errorMessage", "비밀번호 입력값을 모두 채워주세요.");
            return "redirect:/mypage";
        }

        if (!passwordEncoder.matches(cur, member.getPassword())) {
            ra.addFlashAttribute("errorMessage", "현재 비밀번호가 일치하지 않습니다.");
            return "redirect:/mypage";
        }

        if (!nw.equals(cf)) {
            ra.addFlashAttribute("errorMessage", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
            return "redirect:/mypage";
        }

        if (passwordEncoder.matches(nw, member.getPassword())) {
            ra.addFlashAttribute("errorMessage", "새 비밀번호는 기존 비밀번호와 다르게 설정해주세요.");
            return "redirect:/mypage";
        }

        member.setPassword(passwordEncoder.encode(nw));

        ra.addFlashAttribute("successMessage", "비밀번호가 변경되었습니다.");
        return "redirect:/mypage";
    }

}
