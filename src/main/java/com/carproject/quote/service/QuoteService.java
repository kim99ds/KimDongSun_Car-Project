package com.carproject.quote.service;

import com.carproject.car.repository.CarImageRepository;
import com.carproject.quote.dto.QuoteViewDto;
import com.carproject.quote.entity.Quote;
import com.carproject.quote.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("quoteDomainService")
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final CarImageRepository carImageRepository;

    public QuoteViewDto getQuoteView(Long quoteId) {

        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("견적 없음"));

        QuoteViewDto dto = new QuoteViewDto();

        // ✅ Quote에는 model이 없음 → Trim → Variant → Model로 접근
        var model = quote.getTrim().getVariant().getModel();

        dto.setModelName(model.getModelName());
        dto.setTrimName(quote.getTrim().getTrimName());
        dto.setTotalPrice(quote.getTotalPrice());

        // ✅ 모델 기준 + viewType (컬러 무시) : 레포에 실제 존재하는 메서드 사용
        carImageRepository
                .findFirstByModel_ModelIdAndViewTypeOrderByImageIdAsc(
                        model.getModelId(),
                        "exterior"
                )
                .ifPresent(img -> dto.setExteriorImageUrl(normalizeWebPath(img.getImageUrl())));

        return dto;
    }

    private String normalizeWebPath(String url) {
        if (url == null) return null;
        String u = url.trim();
        if (u.isEmpty()) return null;

        u = u.replace("\\", "/");

        if (u.startsWith("http://") || u.startsWith("https://")) return u;
        if (!u.startsWith("/")) u = "/" + u;
        return u;
    }
}
