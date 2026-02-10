package com.carproject.admin.events.dto;

import com.carproject.event.entity.DiscountType;
import com.carproject.event.entity.Event;
import com.carproject.event.entity.EventStatus;
import com.carproject.event.entity.TargetType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AdminEventForms {

    @Getter
    @Setter
    public static class EventForm {

        @NotBlank(message = "필수 작성 사항입니다.")
        private String title;

        @NotBlank(message = "필수 작성 사항입니다.")
        private String description;

        @NotNull(message = "필수 작성 사항입니다.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate startDate;

        @NotNull(message = "필수 작성 사항입니다.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate endDate;

        @NotNull(message = "필수 작성 사항입니다.")
        private EventStatus status;

        private String bannerImage;

        public static EventForm from(Event e) {
            EventForm f = new EventForm();
            f.setTitle(e.getTitle());
            f.setDescription(e.getDescription());
            f.setStartDate(e.getStartDate());
            f.setEndDate(e.getEndDate());
            f.setStatus(e.getStatus());
            f.setBannerImage(e.getBannerImage());
            return f;
        }
    }

    @Getter
    @Setter
    public static class EventCreateForm extends EventForm {

        // 화면에서 최소 1행을 쓰는 구조라면 "존재"도 필수로 두는 게 안전
        @Valid
        @Size(min = 1, message = "필수 작성 사항입니다.")
        private List<TargetForm> targets = new ArrayList<>();

        @Valid
        @Size(min = 1, message = "필수 작성 사항입니다.")
        private List<PolicyForm> policies = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class TargetForm {

        @NotNull(message = "필수 작성 사항입니다.")
        private TargetType targetType;

        @NotBlank(message = "필수 작성 사항입니다.")
        private String targetValue;
    }

    @Getter
    @Setter
    public static class PolicyForm {

        @NotNull(message = "필수 작성 사항입니다.")
        private DiscountType discountType;

        @NotNull(message = "필수 작성 사항입니다.")
        private BigDecimal discountValue;
    }
}
