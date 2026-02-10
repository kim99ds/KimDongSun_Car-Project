package com.carproject.quote.entity;

import com.carproject.event.entity.Event;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "QUOTE_EVENT",
    schema = "CAR_PROJECT",
    uniqueConstraints = @UniqueConstraint(name = "UK_QUOTE_EVENT", columnNames = {"QUOTE_ID", "EVENT_ID"})
)
public class QuoteEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_QUOTE_EVENT_GEN")
    @SequenceGenerator(name = "SEQ_QUOTE_EVENT_GEN", sequenceName = "CAR_PROJECT.SEQ_QUOTE_EVENT", allocationSize = 1)
    @Column(name = "QUOTE_EVENT_ID")
    private Long quoteEventId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "QUOTE_ID", nullable = false)
    private Quote quote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EVENT_ID", nullable = false)
    private Event event;

    @Column(name = "DISCOUNT_PRICE", nullable = false)
    private BigDecimal discountPrice;
}
