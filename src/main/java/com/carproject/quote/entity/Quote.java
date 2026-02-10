package com.carproject.quote.entity;

import com.carproject.car.entity.CarTrim;
import com.carproject.car.entity.TrimColor;
import com.carproject.global.common.entity.BaseTimeEntity;
import com.carproject.member.entity.Member;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "QUOTE", schema = "CAR_PROJECT")
public class Quote extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_QUOTE_GEN")
    @SequenceGenerator(name = "SEQ_QUOTE_GEN", sequenceName = "CAR_PROJECT.SEQ_QUOTE", allocationSize = 1)
    @Column(name = "QUOTE_ID")
    private Long quoteId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TRIM_ID", nullable = false)
    private CarTrim trim;

    // Nullable in DDL
    @Column(name = "TRIM_COLOR_ID")
    private Long trimColorId;

    // Composite FK in DB: (TRIM_ID, TRIM_COLOR_ID) -> TRIM_COLOR(TRIM_ID, TRIM_COLOR_ID)
    // Keep association read-only to avoid column duplication; write via trim + trimColorId.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "TRIM_ID", referencedColumnName = "TRIM_ID", insertable = false, updatable = false),
        @JoinColumn(name = "TRIM_COLOR_ID", referencedColumnName = "TRIM_COLOR_ID", insertable = false, updatable = false)
    })
    private TrimColor trimColor;

    @Column(name = "BASE_PRICE", nullable = false)
    private BigDecimal basePrice;

    @Column(name = "OPTION_PRICE", nullable = false)
    private BigDecimal optionPrice = BigDecimal.ZERO;

    @Column(name = "DISCOUNT_PRICE", nullable = false)
    private BigDecimal discountPrice = BigDecimal.ZERO;

    @Column(name = "TOTAL_PRICE", nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private QuoteStatus status = QuoteStatus.CREATED;

    @OneToMany(mappedBy = "quote", fetch = FetchType.LAZY)
    private List<QuoteOption> quoteOptions = new ArrayList<>();

    @OneToMany(mappedBy = "quote", fetch = FetchType.LAZY)
    private List<QuoteEvent> quoteEvents = new ArrayList<>();
}
