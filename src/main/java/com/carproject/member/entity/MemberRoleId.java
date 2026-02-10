package com.carproject.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class MemberRoleId implements Serializable {

    @Column(name = "MEMBER_ID", nullable = false)
    private Long memberId;

    @Column(name = "ROLE_ID", nullable = false)
    private Long roleId;
}
