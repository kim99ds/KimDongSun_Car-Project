package com.carproject.admin.banner;

import com.carproject.landing.entity.LandingBanner;
import com.carproject.landing.repository.LandingBannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/banners")
public class AdminBannerController {

    private final LandingBannerRepository bannerRepository;
    private final LandingFileStorage landingFileStorage;

    // 목록
    @GetMapping
    public String list(@RequestParam(value = "fragment", required = false) String fragment,
                       Model model) {

        List<LandingBanner> banners = bannerRepository.findAllByOrderBySortOrderAsc();
        model.addAttribute("banners", banners);

        if (fragment != null) {
            return "admin/fragments/banners :: content";
        }

        model.addAttribute("activeMenu", "banners");
        model.addAttribute("contentTemplate", "admin/fragments/banners");
        return "admin/app";
    }

    // 추가 폼
    @GetMapping("/new")
    public String createForm(@RequestParam(value = "fragment", required = false) String fragment,
                             Model model) {
        model.addAttribute("banner", new LandingBanner());

        if (fragment != null) {
            return "admin/fragments/banner-form :: content";
        }
        model.addAttribute("activeMenu", "banners");
        model.addAttribute("contentTemplate", "admin/fragments/banner-form");
        return "admin/app";
    }

    // 수정 폼
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id,
                           @RequestParam(value = "fragment", required = false) String fragment,
                           Model model,
                           RedirectAttributes ra) {

        LandingBanner banner = bannerRepository.findById(id).orElse(null);
        if (banner == null) {
            ra.addFlashAttribute("errorMessage", "배너를 찾을 수 없습니다. (id=" + id + ")");
            return "redirect:/admin/banners";
        }

        model.addAttribute("banner", banner);

        if (fragment != null) {
            return "admin/fragments/banner-form :: content";
        }

        model.addAttribute("activeMenu", "banners");
        model.addAttribute("contentTemplate", "admin/fragments/banner-form");
        return "admin/app";
    }

    // 저장(추가/수정 공용) - 이미지 + 페이지ZIP
    @PostMapping("/save")
    public String save(@ModelAttribute LandingBanner banner,
                       @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                       @RequestParam(value = "pageZip", required = false) MultipartFile pageZip,
                       RedirectAttributes ra) {

        // 1) 이미지 업로드
        if (imageFile != null && !imageFile.isEmpty()) {
            String savedUrl = landingFileStorage.save(imageFile);
            banner.setImageUrl(savedUrl);
        } else {
            // 수정 화면에서 파일 안 올리면 기존 imageUrl 유지
            if (banner.getBannerId() != null) {
                bannerRepository.findById(banner.getBannerId())
                        .ifPresent(old -> {
                            if (banner.getImageUrl() == null || banner.getImageUrl().isBlank()) {
                                banner.setImageUrl(old.getImageUrl());
                            }
                        });
            }
        }

        // 2) HTML 폴더형 페이지 ZIP 업로드 → linkUrl에 저장
        if (pageZip != null && !pageZip.isEmpty()) {
            String pageUrl = landingFileStorage.savePageZip(pageZip);
            banner.setLinkUrl(pageUrl); // ✅ 여기! moreUrl 말고 linkUrl
        } else {
            // 수정 화면에서 zip 안 올리면 기존 linkUrl 유지
            if (banner.getBannerId() != null) {
                bannerRepository.findById(banner.getBannerId())
                        .ifPresent(old -> {
                            if (banner.getLinkUrl() == null || banner.getLinkUrl().isBlank()) {
                                banner.setLinkUrl(old.getLinkUrl());
                            }
                        });
            }
        }

        bannerRepository.save(banner);
        ra.addFlashAttribute("successMessage", "배너가 저장되었습니다.");
        return "redirect:/admin/banners";
    }

    // 노출 토글
    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable("id") Long id,
                         RedirectAttributes ra) {

        bannerRepository.findById(id).ifPresentOrElse(b -> {
            String v = ("Y".equalsIgnoreCase(b.getIsVisible())) ? "N" : "Y";
            b.setIsVisible(v);
            bannerRepository.save(b);
        }, () -> ra.addFlashAttribute("errorMessage", "배너를 찾을 수 없습니다. (id=" + id + ")"));

        ra.addFlashAttribute("successMessage", "노출 여부가 변경되었습니다.");
        return "redirect:/admin/banners";
    }

    // 삭제
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id,
                         RedirectAttributes ra) {
        bannerRepository.deleteById(id);
        ra.addFlashAttribute("successMessage", "배너가 삭제되었습니다.");
        return "redirect:/admin/banners";
    }
}
