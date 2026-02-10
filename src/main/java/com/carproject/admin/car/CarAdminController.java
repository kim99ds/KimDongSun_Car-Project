package com.carproject.admin.car;

import com.carproject.car.entity.*;
import com.carproject.car.repository.*;
import com.carproject.global.common.entity.Yn;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/cars")
public class CarAdminController {

    private final BrandRepository brandRepository;
    private final CarModelRepository carModelRepository;
    private final CarVariantRepository carVariantRepository;
    private final CarTrimRepository carTrimRepository;

    private final OptionItemRepository optionItemRepository;
    private final TrimOptionRepository trimOptionRepository;

    private final CarColorRepository carColorRepository;
    private final TrimColorRepository trimColorRepository;

    private final CarImageRepository carImageRepository;
    private final CarImageStorage carImageStorage;

    private final OptionDependencyRepository optionDependencyRepository;
    private final OptionPackageItemRepository optionPackageItemRepository;

    private final OptionGroupRepository optionGroupRepository;
    private final OptionGroupItemRepository optionGroupItemRepository;

    @GetMapping
    public String page(@RequestParam(value = "fragment", required = false) String fragment,
                       @RequestParam(value = "tab", required = false, defaultValue = "brand") String tab,
                       @RequestParam(value = "editId", required = false) Long editId,
                       Model model) {

        model.addAttribute("tab", tab);

        // ✅ 핵심: edit는 항상 존재하게 (null로라도) 세팅
        model.addAttribute("edit", null);

        // 공통 드롭다운용
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("models", carModelRepository.findAll());
        model.addAttribute("variants", carVariantRepository.findAll());
        model.addAttribute("trims", carTrimRepository.findAll());
        model.addAttribute("optionItems", optionItemRepository.findAll());
        model.addAttribute("colors", carColorRepository.findAll());
        model.addAttribute("optionGroups", optionGroupRepository.findAll());

        // 탭별 리스트
        switch (tab) {
            case "brand" -> model.addAttribute("list", brandRepository.findAll());
            case "model" -> model.addAttribute("list", carModelRepository.findAll());
            case "variant" -> model.addAttribute("list", carVariantRepository.findAll());
            case "trim" -> model.addAttribute("list", carTrimRepository.findAll());
            case "option" -> model.addAttribute("list", optionItemRepository.findAll());
            case "trimOption" -> model.addAttribute("list", trimOptionRepository.findAll());
            case "color" -> model.addAttribute("list", carColorRepository.findAll());
            case "trimColor" -> model.addAttribute("list", trimColorRepository.findAll());
            case "dependency" -> model.addAttribute("list", optionDependencyRepository.findAll());
            case "group" -> model.addAttribute("list", optionGroupRepository.findAll());
            case "groupItem" -> model.addAttribute("list", optionGroupItemRepository.findAll());
            case "packageItem" -> model.addAttribute("list", optionPackageItemRepository.findAll());
            case "carImage" -> model.addAttribute("list", carImageRepository.findAll());
            default -> {
                model.addAttribute("tab", "brand");
                model.addAttribute("list", brandRepository.findAll());
            }
        }

        // ✅ 수정모드: editId가 오면 탭에 맞는 단건을 edit로 실어줌
        if (editId != null) {
            switch (tab) {
                case "brand" -> model.addAttribute("edit",
                        brandRepository.findById(editId)
                                .orElseThrow(() -> new EntityNotFoundException("브랜드 없음")));
                case "model" -> model.addAttribute("edit",
                        carModelRepository.findById(editId)
                                .orElseThrow(() -> new EntityNotFoundException("모델 없음")));
                case "variant" -> model.addAttribute("edit",
                        carVariantRepository.findById(editId)
                                .orElseThrow(() -> new EntityNotFoundException("엔진(Variant) 없음")));
                case "trim" -> model.addAttribute("edit",
                        carTrimRepository.findById(editId)
                                .orElseThrow(() -> new EntityNotFoundException("트림 없음")));
                case "option" -> model.addAttribute("edit",
                        optionItemRepository.findById(editId)
                                .orElseThrow(() -> new EntityNotFoundException("옵션 없음")));
                case "trimOption" -> model.addAttribute("edit",
                        trimOptionRepository.findById(editId)
                                .orElseThrow(() -> new EntityNotFoundException("트림-옵션 없음")));
                case "color" -> model.addAttribute("edit",
                        carColorRepository.findById(editId)
                                .orElseThrow(() -> new EntityNotFoundException("컬러 없음")));
                case "trimColor" -> model.addAttribute("edit",
                        trimColorRepository.findById(editId)
                                .orElseThrow(() -> new EntityNotFoundException("트림-컬러 없음")));
                case "dependency" -> model.addAttribute("edit",
                        optionDependencyRepository.findById(editId)
                                .orElseThrow(() -> new EntityNotFoundException("옵션 제약 없음")));
                case "group" -> model.addAttribute("edit",
                        optionGroupRepository.findById(editId)
                                .orElseThrow(() -> new EntityNotFoundException("옵션 그룹 없음")));
                case "groupItem" -> model.addAttribute("edit",
                        optionGroupItemRepository.findById(editId)
                                .orElseThrow(() -> new EntityNotFoundException("그룹-옵션 없음")));
                case "packageItem" -> model.addAttribute("edit",
                        optionPackageItemRepository.findById(editId)
                                .orElseThrow(() -> new EntityNotFoundException("패키지 구성 없음")));
                case "carImage" -> model.addAttribute("edit",
                        carImageRepository.findById(editId)
                                .orElseThrow(() -> new EntityNotFoundException("이미지 없음")));
            }
        }

        if (fragment != null) {
            return "admin/fragments/cars :: content";
        }

        model.addAttribute("activeMenu", "cars");
        model.addAttribute("contentTemplate", "admin/fragments/cars");
        return "admin/app";
    }

    // =========================
    // BRAND
    // =========================
    @PostMapping("/brand")
    public String createBrand(@RequestParam String brandName,
                              @RequestParam(required = false) String countryCode,
                              RedirectAttributes ra) {

        Brand b = new Brand();
        b.setBrandName(brandName);

        if (countryCode != null && !countryCode.isBlank()) {
            String cc = countryCode.trim().toUpperCase();
            if (cc.length() > 3) cc = cc.substring(0, 3);
            b.setCountryCode(cc);
        } else {
            b.setCountryCode(null);
        }

        brandRepository.save(b);
        ra.addFlashAttribute("successMessage", "브랜드 등록 완료");
        return "redirect:/admin/cars?tab=brand";
    }

    @PostMapping("/brand/{id}/update")
    public String updateBrand(@PathVariable Long id,
                              @RequestParam String brandName,
                              @RequestParam(required = false) String countryCode,
                              RedirectAttributes ra) {

        Brand b = brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("브랜드 없음"));

        b.setBrandName(brandName);

        if (countryCode != null && !countryCode.isBlank()) {
            String cc = countryCode.trim().toUpperCase();
            if (cc.length() > 3) cc = cc.substring(0, 3);
            b.setCountryCode(cc);
        } else {
            b.setCountryCode(null);
        }

        brandRepository.save(b);
        ra.addFlashAttribute("successMessage", "브랜드 수정 완료");
        return "redirect:/admin/cars?tab=brand";
    }

    @PostMapping("/brand/{id}/delete")
    public String deleteBrand(@PathVariable Long id, RedirectAttributes ra) {
        brandRepository.deleteById(id);
        ra.addFlashAttribute("successMessage", "브랜드 삭제 완료");
        return "redirect:/admin/cars?tab=brand";
    }

    // =========================
    // CAR_MODEL
    // =========================
    @PostMapping("/model")
    public String createModel(@RequestParam Long brandId,
                              @RequestParam String modelName,
                              @RequestParam(required = false) Integer modelYear,
                              @RequestParam(required = false) String segment,
                              @RequestParam(required = false) String releaseDate,
                              RedirectAttributes ra) {

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new EntityNotFoundException("브랜드 없음"));

        CarModel m = new CarModel();
        m.setBrand(brand);
        m.setModelName(modelName);
        m.setModelYear(modelYear);
        m.setSegment(segment);
        if (releaseDate != null && !releaseDate.isBlank()) {
            m.setReleaseDate(LocalDate.parse(releaseDate));
        }
        carModelRepository.save(m);

        ra.addFlashAttribute("successMessage", "모델 등록 완료");
        return "redirect:/admin/cars?tab=model";
    }

    @PostMapping("/model/{id}/update")
    public String updateModel(@PathVariable Long id,
                              @RequestParam Long brandId,
                              @RequestParam String modelName,
                              @RequestParam(required = false) Integer modelYear,
                              @RequestParam(required = false) String segment,
                              @RequestParam(required = false) String releaseDate,
                              RedirectAttributes ra) {

        CarModel m = carModelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("모델 없음"));

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new EntityNotFoundException("브랜드 없음"));

        m.setBrand(brand);
        m.setModelName(modelName);
        m.setModelYear(modelYear);
        m.setSegment(segment);

        if (releaseDate != null && !releaseDate.isBlank()) {
            m.setReleaseDate(LocalDate.parse(releaseDate));
        } else {
            m.setReleaseDate(null);
        }

        carModelRepository.save(m);

        ra.addFlashAttribute("successMessage", "모델 수정 완료");
        return "redirect:/admin/cars?tab=model";
    }

    @PostMapping("/model/{id}/delete")
    public String deleteModel(@PathVariable Long id, RedirectAttributes ra) {
        carModelRepository.deleteById(id);
        ra.addFlashAttribute("successMessage", "모델 삭제 완료");
        return "redirect:/admin/cars?tab=model";
    }

    // =========================
    // CAR_VARIANT
    // =========================
    @PostMapping("/variant")
    public String createVariant(@RequestParam Long modelId,
                                @RequestParam String engineType,
                                @RequestParam String engineName,
                                @RequestParam(required = false) Integer displacementCc,
                                @RequestParam(required = false) String transmission,
                                @RequestParam(required = false) String driveType,
                                @RequestParam(required = false) String fuelEfficiency,
                                @RequestParam(required = false) Integer co2Emission,
                                RedirectAttributes ra) {

        CarModel model = carModelRepository.findById(modelId)
                .orElseThrow(() -> new EntityNotFoundException("모델 없음"));

        CarVariant v = new CarVariant();
        v.setModel(model);
        v.setEngineType(engineType);
        v.setEngineName(engineName);
        v.setDisplacementCc(displacementCc);
        v.setTransmission(transmission);
        v.setDriveType(driveType);
        v.setFuelEfficiency(fuelEfficiency);
        v.setCo2Emission(co2Emission);

        carVariantRepository.save(v);

        ra.addFlashAttribute("successMessage", "엔진(Variant) 등록 완료");
        return "redirect:/admin/cars?tab=variant";
    }

    @PostMapping("/variant/{id}/update")
    public String updateVariant(@PathVariable Long id,
                                @RequestParam Long modelId,
                                @RequestParam String engineType,
                                @RequestParam String engineName,
                                @RequestParam(required = false) Integer displacementCc,
                                @RequestParam(required = false) String transmission,
                                @RequestParam(required = false) String driveType,
                                @RequestParam(required = false) String fuelEfficiency,
                                @RequestParam(required = false) Integer co2Emission,
                                RedirectAttributes ra) {

        CarVariant v = carVariantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("엔진(Variant) 없음"));

        CarModel model = carModelRepository.findById(modelId)
                .orElseThrow(() -> new EntityNotFoundException("모델 없음"));

        v.setModel(model);
        v.setEngineType(engineType);
        v.setEngineName(engineName);
        v.setDisplacementCc(displacementCc);
        v.setTransmission(transmission);
        v.setDriveType(driveType);
        v.setFuelEfficiency(fuelEfficiency);
        v.setCo2Emission(co2Emission);

        carVariantRepository.save(v);

        ra.addFlashAttribute("successMessage", "엔진(Variant) 수정 완료");
        return "redirect:/admin/cars?tab=variant";
    }

    @PostMapping("/variant/{id}/delete")
    public String deleteVariant(@PathVariable Long id, RedirectAttributes ra) {
        carVariantRepository.deleteById(id);
        ra.addFlashAttribute("successMessage", "엔진(Variant) 삭제 완료");
        return "redirect:/admin/cars?tab=variant";
    }

    // =========================
    // CAR_TRIM
    // =========================
    @PostMapping("/trim")
    public String createTrim(@RequestParam Long variantId,
                             @RequestParam String trimName,
                             @RequestParam BigDecimal basePrice,
                             @RequestParam(required = false) String description,
                             RedirectAttributes ra) {

        CarVariant variant = carVariantRepository.findById(variantId)
                .orElseThrow(() -> new EntityNotFoundException("엔진(Variant) 없음"));

        CarTrim t = new CarTrim();
        t.setVariant(variant);
        t.setTrimName(trimName);
        t.setBasePrice(basePrice);
        t.setDescription(description);

        carTrimRepository.save(t);

        ra.addFlashAttribute("successMessage", "트림 등록 완료");
        return "redirect:/admin/cars?tab=trim";
    }

    @PostMapping("/trim/{id}/update")
    public String updateTrim(@PathVariable Long id,
                             @RequestParam Long variantId,
                             @RequestParam String trimName,
                             @RequestParam BigDecimal basePrice,
                             @RequestParam(required = false) String description,
                             RedirectAttributes ra) {

        CarTrim t = carTrimRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("트림 없음"));

        CarVariant variant = carVariantRepository.findById(variantId)
                .orElseThrow(() -> new EntityNotFoundException("엔진(Variant) 없음"));

        t.setVariant(variant);
        t.setTrimName(trimName);
        t.setBasePrice(basePrice);
        t.setDescription(description);

        carTrimRepository.save(t);

        ra.addFlashAttribute("successMessage", "트림 수정 완료");
        return "redirect:/admin/cars?tab=trim";
    }

    @PostMapping("/trim/{id}/delete")
    public String deleteTrim(@PathVariable Long id, RedirectAttributes ra) {
        carTrimRepository.deleteById(id);
        ra.addFlashAttribute("successMessage", "트림 삭제 완료");
        return "redirect:/admin/cars?tab=trim";
    }

    // =========================
    // OPTION_ITEM
    // =========================
    @PostMapping("/option")
    public String createOption(@RequestParam Long brandId,
                               @RequestParam String optionName,
                               @RequestParam(required = false) String optionDesc,
                               @RequestParam OptionType optionType,
                               @RequestParam OptionCategory optionCategory,
                               @RequestParam SelectRule selectRule,
                               @RequestParam(required = false) BigDecimal optionPrice,
                               RedirectAttributes ra) {

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new EntityNotFoundException("브랜드 없음"));

        OptionItem oi = new OptionItem();
        oi.setBrand(brand);
        oi.setOptionName(optionName);
        oi.setOptionDesc(optionDesc);
        oi.setOptionType(optionType);
        oi.setOptionCategory(optionCategory);
        oi.setSelectRule(selectRule);
        oi.setOptionPrice(optionPrice == null ? BigDecimal.ZERO : optionPrice);

        optionItemRepository.save(oi);

        ra.addFlashAttribute("successMessage", "옵션 등록 완료");
        return "redirect:/admin/cars?tab=option";
    }

    @PostMapping("/option/{id}/update")
    public String updateOption(@PathVariable Long id,
                               @RequestParam Long brandId,
                               @RequestParam String optionName,
                               @RequestParam(required = false) String optionDesc,
                               @RequestParam OptionType optionType,
                               @RequestParam OptionCategory optionCategory,
                               @RequestParam SelectRule selectRule,
                               @RequestParam(required = false) BigDecimal optionPrice,
                               RedirectAttributes ra) {

        OptionItem oi = optionItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("옵션 없음"));

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new EntityNotFoundException("브랜드 없음"));

        oi.setBrand(brand);
        oi.setOptionName(optionName);
        oi.setOptionDesc(optionDesc);
        oi.setOptionType(optionType);
        oi.setOptionCategory(optionCategory);
        oi.setSelectRule(selectRule);
        oi.setOptionPrice(optionPrice == null ? BigDecimal.ZERO : optionPrice);

        optionItemRepository.save(oi);

        ra.addFlashAttribute("successMessage", "옵션 수정 완료");
        return "redirect:/admin/cars?tab=option";
    }

    @PostMapping("/option/{id}/delete")
    public String deleteOption(@PathVariable Long id, RedirectAttributes ra) {
        optionItemRepository.deleteById(id);
        ra.addFlashAttribute("successMessage", "옵션 삭제 완료");
        return "redirect:/admin/cars?tab=option";
    }

    // =========================
    // TRIM_OPTION
    // =========================
    @PostMapping("/trim-option")
    public String createTrimOption(@RequestParam Long trimId,
                                   @RequestParam Long optionItemId,
                                   @RequestParam Yn isRequired,
                                   RedirectAttributes ra) {

        CarTrim trim = carTrimRepository.findById(trimId)
                .orElseThrow(() -> new EntityNotFoundException("트림 없음"));
        OptionItem optionItem = optionItemRepository.findById(optionItemId)
                .orElseThrow(() -> new EntityNotFoundException("옵션 없음"));

        TrimOption to = new TrimOption();
        to.setTrim(trim);
        to.setOptionItem(optionItem);
        to.setIsRequired(isRequired);

        trimOptionRepository.save(to);

        ra.addFlashAttribute("successMessage", "트림-옵션 연결 등록 완료");
        return "redirect:/admin/cars?tab=trimOption";
    }

    @PostMapping("/trim-option/{id}/update")
    public String updateTrimOption(@PathVariable Long id,
                                   @RequestParam Long trimId,
                                   @RequestParam Long optionItemId,
                                   @RequestParam Yn isRequired,
                                   RedirectAttributes ra) {

        TrimOption to = trimOptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("트림-옵션 없음"));

        CarTrim trim = carTrimRepository.findById(trimId)
                .orElseThrow(() -> new EntityNotFoundException("트림 없음"));
        OptionItem optionItem = optionItemRepository.findById(optionItemId)
                .orElseThrow(() -> new EntityNotFoundException("옵션 없음"));

        to.setTrim(trim);
        to.setOptionItem(optionItem);
        to.setIsRequired(isRequired);

        trimOptionRepository.save(to);

        ra.addFlashAttribute("successMessage", "트림-옵션 연결 수정 완료");
        return "redirect:/admin/cars?tab=trimOption";
    }

    @PostMapping("/trim-option/{id}/delete")
    public String deleteTrimOption(@PathVariable Long id, RedirectAttributes ra) {
        trimOptionRepository.deleteById(id);
        ra.addFlashAttribute("successMessage", "트림-옵션 연결 삭제 완료");
        return "redirect:/admin/cars?tab=trimOption";
    }

    // =========================
    // CAR_COLOR
    // =========================
    @PostMapping("/color")
    public String createColor(@RequestParam String colorName,
                              @RequestParam(required = false) String colorCode,
                              @RequestParam(required = false) BigDecimal colorPrice,
                              @RequestParam String colorType,
                              RedirectAttributes ra) {

        CarColor c = new CarColor();
        c.setColorName(colorName);
        c.setColorCode(colorCode);
        c.setColorPrice(colorPrice == null ? BigDecimal.ZERO : colorPrice);
        c.setColorType(colorType);

        carColorRepository.save(c);

        ra.addFlashAttribute("successMessage", "컬러 등록 완료");
        return "redirect:/admin/cars?tab=color";
    }

    @PostMapping("/color/{id}/update")
    public String updateColor(@PathVariable Long id,
                              @RequestParam String colorName,
                              @RequestParam(required = false) String colorCode,
                              @RequestParam(required = false) BigDecimal colorPrice,
                              @RequestParam String colorType,
                              RedirectAttributes ra) {

        CarColor c = carColorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("컬러 없음"));

        c.setColorName(colorName);
        c.setColorCode(colorCode);
        c.setColorPrice(colorPrice == null ? BigDecimal.ZERO : colorPrice);
        c.setColorType(colorType);

        carColorRepository.save(c);

        ra.addFlashAttribute("successMessage", "컬러 수정 완료");
        return "redirect:/admin/cars?tab=color";
    }

    @PostMapping("/color/{id}/delete")
    public String deleteColor(@PathVariable Long id, RedirectAttributes ra) {
        carColorRepository.deleteById(id);
        ra.addFlashAttribute("successMessage", "컬러 삭제 완료");
        return "redirect:/admin/cars?tab=color";
    }

    // =========================
    // TRIM_COLOR
    // =========================
    @PostMapping("/trim-color")
    public String createTrimColor(@RequestParam Long trimId,
                                  @RequestParam Long colorId,
                                  RedirectAttributes ra) {

        CarTrim trim = carTrimRepository.findById(trimId)
                .orElseThrow(() -> new EntityNotFoundException("트림 없음"));
        CarColor color = carColorRepository.findById(colorId)
                .orElseThrow(() -> new EntityNotFoundException("컬러 없음"));

        TrimColor tc = new TrimColor();
        tc.setTrim(trim);
        tc.setColor(color);

        trimColorRepository.save(tc);

        ra.addFlashAttribute("successMessage", "트림-컬러 연결 등록 완료");
        return "redirect:/admin/cars?tab=trimColor";
    }

    @PostMapping("/trim-color/{id}/update")
    public String updateTrimColor(@PathVariable Long id,
                                  @RequestParam Long trimId,
                                  @RequestParam Long colorId,
                                  RedirectAttributes ra) {

        TrimColor tc = trimColorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("트림-컬러 없음"));

        CarTrim trim = carTrimRepository.findById(trimId)
                .orElseThrow(() -> new EntityNotFoundException("트림 없음"));
        CarColor color = carColorRepository.findById(colorId)
                .orElseThrow(() -> new EntityNotFoundException("컬러 없음"));

        tc.setTrim(trim);
        tc.setColor(color);

        trimColorRepository.save(tc);

        ra.addFlashAttribute("successMessage", "트림-컬러 연결 수정 완료");
        return "redirect:/admin/cars?tab=trimColor";
    }

    @PostMapping("/trim-color/{id}/delete")
    public String deleteTrimColor(@PathVariable Long id, RedirectAttributes ra) {
        trimColorRepository.deleteById(id);
        ra.addFlashAttribute("successMessage", "트림-컬러 연결 삭제 완료");
        return "redirect:/admin/cars?tab=trimColor";
    }

    // =========================
    // OPTION_DEPENDENCY
    // =========================
    @PostMapping("/dependency")
    public String createDependency(@RequestParam Long optionItemId,
                                   @RequestParam Long relatedOptionItemId,
                                   @RequestParam DependencyRuleType ruleType,
                                   RedirectAttributes ra) {

        OptionItem oi1 = optionItemRepository.findById(optionItemId)
                .orElseThrow(() -> new EntityNotFoundException("옵션 없음"));
        OptionItem oi2 = optionItemRepository.findById(relatedOptionItemId)
                .orElseThrow(() -> new EntityNotFoundException("연관옵션 없음"));

        OptionDependency d = new OptionDependency();
        d.setOptionItem(oi1);
        d.setRelatedOptionItem(oi2);
        d.setRuleType(ruleType);

        optionDependencyRepository.save(d);

        ra.addFlashAttribute("successMessage", "옵션 제약 등록 완료");
        return "redirect:/admin/cars?tab=dependency";
    }

    @PostMapping("/dependency/{id}/update")
    public String updateDependency(@PathVariable Long id,
                                   @RequestParam Long optionItemId,
                                   @RequestParam Long relatedOptionItemId,
                                   @RequestParam DependencyRuleType ruleType,
                                   RedirectAttributes ra) {

        OptionDependency d = optionDependencyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("옵션 제약 없음"));

        OptionItem oi1 = optionItemRepository.findById(optionItemId)
                .orElseThrow(() -> new EntityNotFoundException("옵션 없음"));
        OptionItem oi2 = optionItemRepository.findById(relatedOptionItemId)
                .orElseThrow(() -> new EntityNotFoundException("연관옵션 없음"));

        d.setOptionItem(oi1);
        d.setRelatedOptionItem(oi2);
        d.setRuleType(ruleType);

        optionDependencyRepository.save(d);

        ra.addFlashAttribute("successMessage", "옵션 제약 수정 완료");
        return "redirect:/admin/cars?tab=dependency";
    }

    @PostMapping("/dependency/{id}/delete")
    public String deleteDependency(@PathVariable Long id, RedirectAttributes ra) {
        optionDependencyRepository.deleteById(id);
        ra.addFlashAttribute("successMessage", "옵션 제약 삭제 완료");
        return "redirect:/admin/cars?tab=dependency";
    }

    // =========================
    // OPTION_GROUP
    // =========================
    @PostMapping("/group")
    public String createGroup(@RequestParam Long brandId,
                              @RequestParam String groupName,
                              @RequestParam SelectRule selectRule,
                              @RequestParam(required = false) String description,
                              RedirectAttributes ra) {

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new EntityNotFoundException("브랜드 없음"));

        OptionGroup g = new OptionGroup();
        g.setBrand(brand);
        g.setGroupName(groupName);
        g.setSelectRule(selectRule);
        g.setDescription(description);

        optionGroupRepository.save(g);

        ra.addFlashAttribute("successMessage", "옵션 그룹 등록 완료");
        return "redirect:/admin/cars?tab=group";
    }

    @PostMapping("/group/{id}/update")
    public String updateGroup(@PathVariable Long id,
                              @RequestParam Long brandId,
                              @RequestParam String groupName,
                              @RequestParam SelectRule selectRule,
                              @RequestParam(required = false) String description,
                              RedirectAttributes ra) {

        OptionGroup g = optionGroupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("옵션 그룹 없음"));

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new EntityNotFoundException("브랜드 없음"));

        g.setBrand(brand);
        g.setGroupName(groupName);
        g.setSelectRule(selectRule);
        g.setDescription(description);

        optionGroupRepository.save(g);

        ra.addFlashAttribute("successMessage", "옵션 그룹 수정 완료");
        return "redirect:/admin/cars?tab=group";
    }

    @PostMapping("/group/{id}/delete")
    public String deleteGroup(@PathVariable Long id, RedirectAttributes ra) {
        optionGroupRepository.deleteById(id);
        ra.addFlashAttribute("successMessage", "옵션 그룹 삭제 완료");
        return "redirect:/admin/cars?tab=group";
    }

    // =========================
    // OPTION_GROUP_ITEM
    // =========================
    @PostMapping("/group-item")
    public String createGroupItem(@RequestParam Long optionGroupId,
                                  @RequestParam Long optionItemId,
                                  RedirectAttributes ra) {

        OptionGroup group = optionGroupRepository.findById(optionGroupId)
                .orElseThrow(() -> new EntityNotFoundException("그룹 없음"));
        OptionItem item = optionItemRepository.findById(optionItemId)
                .orElseThrow(() -> new EntityNotFoundException("옵션 없음"));

        OptionGroupItem gi = new OptionGroupItem();
        gi.setOptionGroup(group);
        gi.setOptionItem(item);

        optionGroupItemRepository.save(gi);

        ra.addFlashAttribute("successMessage", "그룹-옵션 등록 완료");
        return "redirect:/admin/cars?tab=groupItem";
    }

    @PostMapping("/group-item/{id}/update")
    public String updateGroupItem(@PathVariable Long id,
                                  @RequestParam Long optionGroupId,
                                  @RequestParam Long optionItemId,
                                  RedirectAttributes ra) {

        OptionGroupItem gi = optionGroupItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("그룹-옵션 없음"));

        OptionGroup group = optionGroupRepository.findById(optionGroupId)
                .orElseThrow(() -> new EntityNotFoundException("그룹 없음"));
        OptionItem item = optionItemRepository.findById(optionItemId)
                .orElseThrow(() -> new EntityNotFoundException("옵션 없음"));

        gi.setOptionGroup(group);
        gi.setOptionItem(item);

        optionGroupItemRepository.save(gi);

        ra.addFlashAttribute("successMessage", "그룹-옵션 수정 완료");
        return "redirect:/admin/cars?tab=groupItem";
    }

    @PostMapping("/group-item/{id}/delete")
    public String deleteGroupItem(@PathVariable Long id, RedirectAttributes ra) {
        optionGroupItemRepository.deleteById(id);
        ra.addFlashAttribute("successMessage", "그룹-옵션 삭제 완료");
        return "redirect:/admin/cars?tab=groupItem";
    }

    // =========================
    // OPTION_PACKAGE_ITEM
    // =========================
    @PostMapping("/package-item")
    public String createPackageItem(@RequestParam Long packageOptionItemId,
                                    @RequestParam Long childOptionItemId,
                                    @RequestParam Yn isIncluded,
                                    @RequestParam(required = false) Integer sortOrder,
                                    RedirectAttributes ra) {

        OptionItem pkg = optionItemRepository.findById(packageOptionItemId)
                .orElseThrow(() -> new EntityNotFoundException("패키지 옵션 없음"));
        OptionItem child = optionItemRepository.findById(childOptionItemId)
                .orElseThrow(() -> new EntityNotFoundException("자식 옵션 없음"));

        OptionPackageItem pi = new OptionPackageItem();
        pi.setPackageOptionItem(pkg);
        pi.setChildOptionItem(child);
        pi.setIsIncluded(isIncluded);
        pi.setSortOrder(sortOrder);

        optionPackageItemRepository.save(pi);

        ra.addFlashAttribute("successMessage", "패키지 구성 등록 완료");
        return "redirect:/admin/cars?tab=packageItem";
    }

    @PostMapping("/package-item/{id}/update")
    public String updatePackageItem(@PathVariable Long id,
                                    @RequestParam Long packageOptionItemId,
                                    @RequestParam Long childOptionItemId,
                                    @RequestParam Yn isIncluded,
                                    @RequestParam(required = false) Integer sortOrder,
                                    RedirectAttributes ra) {

        OptionPackageItem pi = optionPackageItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("패키지 구성 없음"));

        OptionItem pkg = optionItemRepository.findById(packageOptionItemId)
                .orElseThrow(() -> new EntityNotFoundException("패키지 옵션 없음"));
        OptionItem child = optionItemRepository.findById(childOptionItemId)
                .orElseThrow(() -> new EntityNotFoundException("자식 옵션 없음"));

        pi.setPackageOptionItem(pkg);
        pi.setChildOptionItem(child);
        pi.setIsIncluded(isIncluded);
        pi.setSortOrder(sortOrder);

        optionPackageItemRepository.save(pi);

        ra.addFlashAttribute("successMessage", "패키지 구성 수정 완료");
        return "redirect:/admin/cars?tab=packageItem";
    }

    @PostMapping("/package-item/{id}/delete")
    public String deletePackageItem(@PathVariable Long id, RedirectAttributes ra) {
        optionPackageItemRepository.deleteById(id);
        ra.addFlashAttribute("successMessage", "패키지 구성 삭제 완료");
        return "redirect:/admin/cars?tab=packageItem";
    }

    // =========================
    // CAR_IMAGE (관리자에서 이미지 등록/수정/삭제)
    // - VIEW_TYPE은 exterior / con 만 허용
    // - 업로드 시 폴더 구조: uploads/car-images/{modelId}/{viewType}/...
    // =========================
    @PostMapping(value = "/car-image", consumes = {"multipart/form-data"})
    public String createCarImage(@RequestParam Long modelId,
                                 @RequestParam String viewType,
                                 @RequestParam("imageFile") MultipartFile imageFile,
                                 RedirectAttributes ra) {

        CarModel model = carModelRepository.findById(modelId)
                .orElseThrow(() -> new EntityNotFoundException("모델 없음"));

        String vt = CarImageStorage.normalizeViewType(viewType);
        String url = carImageStorage.save(imageFile, modelId, vt);

        CarImage img = new CarImage();
        img.setModel(model);
        img.setViewType(vt);
        img.setImageUrl(url);

        carImageRepository.save(img);

        ra.addFlashAttribute("successMessage", "이미지 등록 완료");
        return "redirect:/admin/cars?tab=carImage";
    }

    @PostMapping(value = "/car-image/{id}/update", consumes = {"multipart/form-data"})
    public String updateCarImage(@PathVariable Long id,
                                 @RequestParam Long modelId,
                                 @RequestParam String viewType,
                                 @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                 RedirectAttributes ra) {

        CarImage img = carImageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("이미지 없음"));

        CarModel model = carModelRepository.findById(modelId)
                .orElseThrow(() -> new EntityNotFoundException("모델 없음"));

        CarColor color = carColorRepository.findById(1L)
                .orElseThrow(() -> new EntityNotFoundException("COLOR_ID=1 컬러가 필요합니다."));

        String vt = CarImageStorage.normalizeViewType(viewType);

        // 파일이 오면 새로 저장하고, 기존 파일도 정리
        if (imageFile != null && !imageFile.isEmpty()) {
            String newUrl = carImageStorage.save(imageFile, modelId, vt);
            carImageStorage.deleteIfLocal(img.getImageUrl());
            img.setImageUrl(newUrl);
        }

        img.setModel(model);
        img.setViewType(vt);

        carImageRepository.save(img);

        ra.addFlashAttribute("successMessage", "이미지 수정 완료");
        return "redirect:/admin/cars?tab=carImage";
    }

    @PostMapping("/car-image/{id}/delete")
    public String deleteCarImage(@PathVariable Long id, RedirectAttributes ra) {
        CarImage img = carImageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("이미지 없음"));
        carImageStorage.deleteIfLocal(img.getImageUrl());
        carImageRepository.deleteById(id);
        ra.addFlashAttribute("successMessage", "이미지 삭제 완료");
        return "redirect:/admin/cars?tab=carImage";
    }
}
