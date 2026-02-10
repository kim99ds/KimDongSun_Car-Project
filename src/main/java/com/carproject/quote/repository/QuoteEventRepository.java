package com.carproject.quote.repository;

import com.carproject.quote.entity.QuoteEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteEventRepository extends JpaRepository<QuoteEvent, Long> {

    void deleteByQuote_QuoteId(Long quoteId);
}
