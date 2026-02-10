package com.carproject.admin.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ✅ 주의:
 * - /admin/cars 는 이제 com.carproject.admin.car.CarAdminController 가 담당합니다.
 * - 여기(AdminDashboardController)에서 /admin/cars 매핑을 유지하면 "Ambiguous mapping"으로 서버가 부팅 실패합니다.
 */
@Controller
public class AdminDashboardController {

    @GetMapping("/admin")
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(@RequestParam(value = "fragment", required = false) String fragment,
                            Model model) {

        // ✅ admin/app.html 내부에서 내용만 교체되도록 fragment 모드 지원
        if (fragment != null) {
            return "admin/fragments/dashboard :: content";
        }

        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("contentTemplate", "admin/fragments/dashboard");
        return "admin/app";
    }


    /**
     * ❌ 기존에 있던 /admin/cars 매핑은 제거했습니다.
     * - /admin/cars 는 CarAdminController 에서 처리
     */

    @GetMapping("/admin/events")
    public String events(@RequestParam(value = "fragment", required = false) String fragment,
                         Model model) {
        if (fragment != null) {
            return "admin/fragments/events :: content";
        }
        model.addAttribute("activeMenu", "events");
        model.addAttribute("contentTemplate", "admin/fragments/events");
        return "admin/app";
    }
}
