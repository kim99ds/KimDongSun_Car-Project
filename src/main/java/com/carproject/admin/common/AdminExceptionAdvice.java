package com.carproject.admin.common;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.format.DateTimeParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@ControllerAdvice(basePackages = "com.carproject.admin")
public class AdminExceptionAdvice {

    @ExceptionHandler(EntityNotFoundException.class)
    public String handleNotFound(EntityNotFoundException e, HttpServletRequest req, RedirectAttributes ra) {
        if (!isAdminRequest(req)) return forwardToError(e, req);
        return redirectWithError(req, ra, e.getMessage() == null ? "대상을 찾을 수 없습니다." : e.getMessage(), e);
    }

    @ExceptionHandler({
            DateTimeParseException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class
    })
    public String handleBadRequest(Exception e, HttpServletRequest req, RedirectAttributes ra) {
        if (!isAdminRequest(req)) return forwardToError(e, req);

        String msg = e.getMessage();
        if (e instanceof DateTimeParseException) {
            msg = "날짜 형식이 올바르지 않습니다. 예) 2026-01-29";
        } else if (e instanceof MethodArgumentTypeMismatchException) {
            msg = "요청 값 형식이 올바르지 않습니다.";
        } else if (msg == null || msg.isBlank()) {
            msg = "요청 값이 올바르지 않습니다.";
        }
        return redirectWithError(req, ra, msg, e);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleIntegrity(DataIntegrityViolationException e, HttpServletRequest req, RedirectAttributes ra) {
        if (!isAdminRequest(req)) return forwardToError(e, req);

        String msg = "DB 제약조건 위반입니다. (중복/참조/필수값/허용값을 확인하세요)";
        return redirectWithError(req, ra, msg, e);
    }

    @ExceptionHandler(Exception.class)
    public String handleEtc(Exception e, HttpServletRequest req, RedirectAttributes ra) {
        if (!isAdminRequest(req)) return forwardToError(e, req);

        String msg = "처리 중 오류가 발생했습니다. 입력값을 확인해주세요.";
        return redirectWithError(req, ra, msg, e);
    }

    private boolean isAdminRequest(HttpServletRequest req) {
        if (req == null) return false;
        String uri = req.getRequestURI();
        return uri != null && uri.startsWith("/admin");
    }

    private String forwardToError(Exception e, HttpServletRequest req) {
        // 여기 들어오면 "관리자 예외 처리 로직이 전체 요청에 적용되는 문제"를 차단한 것
        log.warn("[ADMIN-ADVICE-BYPASS] uri={} ex={}", (req == null ? null : req.getRequestURI()), e.toString());
        return "forward:/error";
    }

    private String redirectWithError(HttpServletRequest req, RedirectAttributes ra, String msg, Exception e) {
        log.warn("[ADMIN] error uri={} msg={}", req.getRequestURI(), msg, e);
        ra.addFlashAttribute("errorMessage", msg);
        return "redirect:/admin/cars?tab=" + guessTab(req);
    }

    private String guessTab(HttpServletRequest req) {
        String tab = req.getParameter("tab");
        if (tab != null && !tab.isBlank()) return tab;

        String uri = req.getRequestURI();
        if (uri == null) return "brand";

        if (uri.contains("trim-option")) return "trimOption";
        if (uri.contains("trim-color")) return "trimColor";
        if (uri.contains("package-item")) return "packageItem";
        if (uri.contains("group-item")) return "groupItem";
        if (uri.contains("/group")) return "group";
        if (uri.contains("/dependency")) return "dependency";
        if (uri.contains("/variant-deny")) return "variantDeny";
        if (uri.contains("/option")) return "option";
        if (uri.contains("/color")) return "color";
        if (uri.contains("/trim")) return "trim";
        if (uri.contains("/variant")) return "variant";
        if (uri.contains("/model")) return "model";
        if (uri.contains("/brand")) return "brand";

        return "brand";
    }
}
