package com.carproject.quote.repository;

import com.carproject.car.dto.QuoteListItemDto;
import com.carproject.quote.entity.Quote;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    @Query("""
        select new com.carproject.car.dto.QuoteListItemDto(
            q.quoteId,
            m.modelId,
            m.modelName,
            t.trimName,
            q.totalPrice,
            q.status,
            q.createdAt
        )
        from Quote q
            join q.trim t
            join t.variant v
            join v.model m
        where q.member.memberId = :memberId
        order by q.createdAt desc
    """)
    List<QuoteListItemDto> findMyQuoteList(@Param("memberId") Long memberId);
}
