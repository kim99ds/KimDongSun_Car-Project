package com.carproject.member.entity;

import com.carproject.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor // Builder 사용을 위해 추가
@Builder // Member.builder() 사용을 위해 추가
@Entity
@Table(
        name = "MEMBER",
        schema = "CAR_PROJECT",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_MEMBER_LOGIN_ID", columnNames = {"LOGIN_ID"}),
                @UniqueConstraint(name = "UK_MEMBER_EMAIL", columnNames = {"EMAIL"})
        }
)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MEMBER_GEN")
    @SequenceGenerator(name = "SEQ_MEMBER_GEN", sequenceName = "CAR_PROJECT.SEQ_MEMBER", allocationSize = 1)
    @Column(name = "MEMBER_ID")
    private Long memberId;

    @Column(name = "LOGIN_ID", nullable = false, length = 100)
    private String loginId;

    @Column(name = "PASSWORD", nullable = false, length = 200)
    private String password;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "EMAIL", nullable = false, length = 200)
    private String email;

    @Column(name = "BIRTH_DATE", nullable = false)
    private LocalDate birthDate;

    @Builder.Default // Builder 사용 시 초기값 유지
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private MemberStatus status = MemberStatus.ACTIVE;

    @Builder.Default
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<MemberRole> memberRoles = new ArrayList<>();

    // --- 추가된 메서드들 ---

    /**
     * 회원 생성을 위한 정적 팩토리 메서드 (MemberService.java:35 대응)
     */
    public static Member create(String loginId, String password, String name, String email, LocalDate birthDate) {
        return Member.builder()
                .loginId(loginId)
                .password(password)
                .name(name)
                .email(email)
                .birthDate(birthDate)
                .status(MemberStatus.ACTIVE)
                .build();
    }

    /**
     * 서비스 계층에서 getId()로 호출하는 경우가 있어 추가 (MemberService.java:48 대응)
     */
    public Long getId() {
        return this.memberId;
    }
}