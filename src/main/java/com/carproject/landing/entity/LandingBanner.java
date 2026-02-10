package com.carproject.landing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "LANDING_BANNER")
@Getter
@Setter
public class LandingBanner {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "landing_banner_seq")
    @SequenceGenerator(
            name = "landing_banner_seq",
            sequenceName = "SEQ_LANDING_BANNER",
            allocationSize = 1
    )
    @Column(name = "BANNER_ID")
    private Long bannerId;

    @Column(name = "TITLE", length = 100)
    private String title;

    @Column(name = "SUB_TITLE", length = 200)
    private String subTitle;

    @Column(name = "IMAGE_URL", length = 300)
    private String imageUrl;

    @Column(name = "LINK_URL", length = 300)
    private String linkUrl;

    /**
     * 'Y' or 'N'
     */
    @Column(name = "IS_VISIBLE", length = 1)
    private String isVisible = "Y";

    @Column(name = "SORT_ORDER")
    private Integer sortOrder = 0;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (isVisible == null) isVisible = "Y";
        if (sortOrder == null) sortOrder = 0;
    }
}
