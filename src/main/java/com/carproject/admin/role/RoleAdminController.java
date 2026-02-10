package com.carproject.admin.role;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/roles")
public class RoleAdminController {

    private final RoleAdminService roleAdminService;

    public RoleAdminController(RoleAdminService roleAdminService) {
        this.roleAdminService = roleAdminService;
    }

    @GetMapping
    public String roles(@RequestParam(value = "fragment", required = false) String fragment,
                        Model model) {
        model.addAttribute("allRoles", roleAdminService.allRoles());
        model.addAttribute("rows", roleAdminService.rows());

        if (fragment != null) {
            return "admin/fragments/roles :: content";
        }
        model.addAttribute("activeMenu", "roles");
        model.addAttribute("contentTemplate", "admin/fragments/roles");
        return "admin/app";
    }

    @PostMapping("/{memberId}")
    public String assign(@PathVariable Long memberId,
                         @RequestParam(value = "roleIds", required = false) List<Long> roleIds,
                         RedirectAttributes ra) {
        try {
            roleAdminService.assignRoles(memberId, roleIds);
            ra.addFlashAttribute("successMessage", "역할 저장 완료");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/roles";
    }

    @PostMapping("/create")
    public String createRole(@RequestParam("roleName") String roleName,
                             RedirectAttributes ra) {
        try {
            roleAdminService.createRole(roleName);
            ra.addFlashAttribute("successMessage", "역할이 추가되었습니다: " + roleName);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/roles";
    }

    @PostMapping("/delete/{roleId}")
    public String deleteRole(@PathVariable Long roleId,
                             RedirectAttributes ra) {
        try {
            roleAdminService.deleteRole(roleId);
            ra.addFlashAttribute("successMessage", "역할이 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/roles";
    }
}
