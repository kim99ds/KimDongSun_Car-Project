package com.carproject.member.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor // link 메서드에서 생성자 편의를 위해 추가
@Entity
@Table(name = "MEMBER_ROLE", schema = "CAR_PROJECT")
public class MemberRole {

    @EmbeddedId
    private MemberRoleId id;

    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member member;

    @MapsId("roleId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ROLE_ID", nullable = false)
    private Role role;

    /**
     * 회원과 권한을 연결해주는 정적 팩토리 메서드 (AdminInitializer, MemberService 대응)
     */
    public static MemberRole link(Member member, Role role) {
        MemberRole memberRole = new MemberRole();
        // 복합키(EmbeddedId) 생성 및 설정
        memberRole.id = new MemberRoleId(member.getMemberId(), role.getRoleId());
        memberRole.member = member;
        memberRole.role = role;
        return memberRole;
    }
}