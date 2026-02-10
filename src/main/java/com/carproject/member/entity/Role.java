package com.carproject.member.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "ROLE",
    schema = "CAR_PROJECT",
    uniqueConstraints = @UniqueConstraint(name = "UK_ROLE_NAME", columnNames = {"ROLE_NAME"})
)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_ROLE_GEN")
    @SequenceGenerator(name = "SEQ_ROLE_GEN", sequenceName = "CAR_PROJECT.SEQ_ROLE", allocationSize = 1)
    @Column(name = "ROLE_ID")
    private Long roleId;

    @Column(name = "ROLE_NAME", nullable = false, length = 50)
    private String roleName;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private List<MemberRole> memberRoles = new ArrayList<>();
}
