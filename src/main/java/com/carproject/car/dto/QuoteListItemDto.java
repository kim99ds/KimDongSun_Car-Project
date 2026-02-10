package com.carproject.car.dto;

import com.carproject.quote.entity.QuoteStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 내 견적서 목록 화면에서 사용하는 최소 DTO
 */
@Getter
@Setter
public class QuoteListItemDto {

    private final Long quoteId;
    private final Long modelId;
    private final String modelName;
    private final String trimName;
    private final BigDecimal totalPrice;
    private final QuoteStatus status;
    private final LocalDateTime createdAt;
    private String imageUrl;
    private String optionSummary;

    public QuoteListItemDto(
            Long quoteId,
            Long modelId,
            String modelName,
            String trimName,
            BigDecimal totalPrice,
            QuoteStatus status,
            LocalDateTime createdAt

    ) {
        this.quoteId = quoteId;
        this.modelId = modelId;
        this.modelName = modelName;
        this.trimName = trimName;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = createdAt;

    }
}
