package com.carproject.quote.entity;

import com.carproject.car.entity.OptionItem;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "QUOTE_OPTION", schema = "CAR_PROJECT")
public class QuoteOption {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_QUOTE_OPTION_GEN")
    @SequenceGenerator(
            name = "SEQ_QUOTE_OPTION_GEN",
            sequenceName = "CAR_PROJECT.SEQ_QUOTE_OPTION",
            allocationSize = 1
    )
    @Column(name = "QUOTE_OPTION_ID")
    private Long quoteOptionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "QUOTE_ID", nullable = false)
    private Quote quote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "OPTION_ITEM_ID", nullable = false)
    private OptionItem optionItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PACKAGE_OPTION_ITEM_ID")
    private OptionItem packageOptionItem;

    @Column(name = "OPTION_PRICE", nullable = false)
    private BigDecimal optionPrice = BigDecimal.ZERO;
}
