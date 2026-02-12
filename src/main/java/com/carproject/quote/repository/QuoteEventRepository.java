package com.carproject.quote.repository;

import com.carproject.quote.entity.QuoteEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteEventRepository extends JpaRepository<QuoteEvent, Long> {

    void deleteByQuote_QuoteId(Long quoteId);

    // ✅ 테스트/검증용 (그리고 실무에서도 유용)
    long countByQuote_QuoteId(Long quoteId);
}
