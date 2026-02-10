package com.carproject.quote.repository;

import com.carproject.quote.entity.QuoteOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteOptionRepository extends JpaRepository<QuoteOption, Long> {

    List<QuoteOption> findByQuote_QuoteId(Long quoteId);

    void deleteByQuote_QuoteId(Long quoteId);
}
