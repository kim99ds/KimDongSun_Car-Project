package com.carproject.car.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class ModelLikeId implements Serializable {

    @Column(name = "MODEL_ID")
    private Long modelId;

    @Column(name = "MEMBER_ID")
    private Long memberId;
}
