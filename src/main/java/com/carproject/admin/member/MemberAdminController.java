package com.carproject.admin.member;

import com.carproject.member.entity.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/members")
public class MemberAdminController {

    private final MemberAdminService memberAdminService;

    public MemberAdminController(MemberAdminService memberAdminService) {
        this.memberAdminService = memberAdminService;
    }

    @GetMapping
    public String list(@RequestParam(value = "fragment", required = false) String fragment,
                       @RequestParam(value = "loginId", required = false) String loginId,
                       @PageableDefault(size = 20) Pageable pageable,
                       Model model) {

        Page<?> members = memberAdminService.searchByLoginId(loginId, pageable);

        model.addAttribute("members", members);
        model.addAttribute("loginId", loginId); // ✅ 검색값 유지

        // ✅ admin/app.html 내부에서 내용만 교체되도록 fragment 모드 지원
        if (fragment != null) {
            return "admin/fragments/members-list :: content";
        }

        model.addAttribute("activeMenu", "members");
        model.addAttribute("contentTemplate", "admin/fragments/members-list");
        return "admin/app";
    }

    @GetMapping("/{memberId}")
    public String detail(@PathVariable Long memberId,
                         @RequestParam(value = "fragment", required = false) String fragment,
                         Model model) {
        model.addAttribute("member", memberAdminService.findOne(memberId));

        var roles = memberAdminService.roles(memberId);
        model.addAttribute("roles", roles);
        boolean isAdmin = roles.stream().anyMatch(mr -> "ADMIN".equals(mr.getRole().getRoleName()));
        model.addAttribute("isAdmin", isAdmin);
        boolean isUser = roles.stream().anyMatch(mr -> "USER".equals(mr.getRole().getRoleName()));
        model.addAttribute("isUser", isUser);

        if (fragment != null) {
            return "admin/fragments/member-detail :: content";
        }

        model.addAttribute("activeMenu", "members");
        model.addAttribute("contentTemplate", "admin/fragments/member-detail");
        return "admin/app";
    }

    @PostMapping("/{memberId}/status")
    public String changeStatus(@PathVariable Long memberId,
                               @RequestParam MemberStatus status,
                               RedirectAttributes ra) {
        memberAdminService.changeStatus(memberId, status);
        ra.addFlashAttribute("successMessage", "상태가 변경되었습니다: " + status);
        return "redirect:/admin/members/" + memberId;
    }

    /**
     * ✅ 관리자 상세에서 이름/이메일 수정 저장
     */
    @PostMapping("/{memberId}/update")
    public String update(@PathVariable Long memberId,
                         @RequestParam String name,
                         @RequestParam String email,
                         RedirectAttributes ra) {
        try {
            memberAdminService.updateNameEmail(memberId, name, email);
            ra.addFlashAttribute("successMessage", "저장 완료!");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/members/" + memberId;
    }

    // ✅ 주소창으로 /update 직접 치는 GET 접근은 detail로 돌리기
    @GetMapping("/{memberId}/update")
    public String blockUpdateGet(@PathVariable Long memberId) {
        return "redirect:/admin/members/" + memberId;
    }

    /**
     * ✅ 회원 삭제 (리스트에서 삭제 버튼)
     */
    @PostMapping("/{memberId}/delete")
    public String delete(@PathVariable Long memberId, RedirectAttributes ra) {
        try {
            memberAdminService.deleteMember(memberId);
            ra.addFlashAttribute("successMessage", "삭제 완료");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/members";
    }
}
