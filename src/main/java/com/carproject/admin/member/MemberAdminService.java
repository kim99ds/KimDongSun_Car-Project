package com.carproject.admin.member;

import com.carproject.member.entity.Member;
import com.carproject.member.entity.MemberRole;
import com.carproject.member.entity.MemberStatus;
import com.carproject.member.repository.MemberRepository;
import com.carproject.member.repository.MemberRoleRepository;
import com.carproject.member.repository.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MemberAdminService {

    private final MemberRepository memberRepository;
    private final MemberRoleRepository memberRoleRepository;
    private final RoleRepository roleRepository;

    public MemberAdminService(MemberRepository memberRepository,
                              MemberRoleRepository memberRoleRepository,
                              RoleRepository roleRepository) {
        this.memberRepository = memberRepository;
        this.memberRoleRepository = memberRoleRepository;
        this.roleRepository = roleRepository;
    }

    // ✅ 관리자 목록: 로그인ID 부분검색 + 페이징
    public Page<Member> searchByLoginId(String loginId, Pageable pageable) {
        if (loginId == null || loginId.isBlank()) {
            return memberRepository.findByStatusNot(MemberStatus.DELETED, pageable);
        }
        return memberRepository.findByLoginIdContainingAndStatusNot(
                loginId.trim(),
                MemberStatus.DELETED,
                pageable
        );
    }


    // (기존 메서드 유지가 필요하면 남겨도 됨)
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Member findOne(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow();
    }

    public List<MemberRole> roles(Long memberId) {
        return memberRoleRepository.findByMember_MemberId(memberId);
    }

    @Transactional
    public void changeStatus(Long memberId, MemberStatus status) {
        Member m = findOne(memberId);
        m.setStatus(status);
    }

    @Transactional
    public void grantAdmin(Long memberId) {
        if (memberRoleRepository.existsByMember_MemberIdAndRole_RoleName(memberId, "ADMIN")) return;

        Member member = findOne(memberId);
        var adminRole = roleRepository.findByRoleName("ADMIN").orElseThrow();
        memberRoleRepository.save(MemberRole.link(member, adminRole));
    }

    @Transactional
    public void revokeAdmin(Long memberId) {
        memberRoleRepository.deleteByMember_MemberIdAndRole_RoleName(memberId, "ADMIN");
    }

    @Transactional
    public void updateNameEmail(Long memberId, String name, String email) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("이름을 입력하세요.");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("이메일을 입력하세요.");

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        String n = name.trim();
        String e = email.trim();

        memberRepository.findByEmail(e)
                .filter(other -> !other.getMemberId().equals(memberId))
                .ifPresent(other -> { throw new IllegalArgumentException("이미 사용 중인 이메일"); });

        member.setName(n);
        member.setEmail(e);
    }

    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        // ✅ 1) 역할 매핑 먼저 제거 (역할관리 화면에 남는 문제 해결)
        memberRoleRepository.deleteByMember_MemberId(memberId);

        // ✅ 2) Soft Delete
        member.setStatus(MemberStatus.DELETED);
    }

}
