package com.carproject.admin.role;

import com.carproject.member.entity.Member;
import com.carproject.member.entity.MemberRole;
import com.carproject.member.entity.Role;
import com.carproject.member.repository.MemberRepository;
import com.carproject.member.repository.MemberRoleRepository;
import com.carproject.member.repository.RoleRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class RoleAdminService {

    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final MemberRoleRepository memberRoleRepository;

    public RoleAdminService(MemberRepository memberRepository,
                            RoleRepository roleRepository,
                            MemberRoleRepository memberRoleRepository) {
        this.memberRepository = memberRepository;
        this.roleRepository = roleRepository;
        this.memberRoleRepository = memberRoleRepository;
    }

    public List<Role> allRoles() {
        return roleRepository.findAll();
    }

    @Transactional
    public Role createRole(String roleNameRaw) {
        if (roleNameRaw == null) {
            throw new IllegalArgumentException("ROLE_NAME은 필수입니다.");
        }

        String roleName = roleNameRaw.trim();
        if (roleName.isEmpty()) {
            throw new IllegalArgumentException("ROLE_NAME은 공백일 수 없습니다.");
        }
        if (roleName.length() > 50) {
            throw new IllegalArgumentException("ROLE_NAME은 50자를 초과할 수 없습니다.");
        }

        // 프로젝트 전반에서 'ADMIN', 'USER' 같은 형태로 쓰는 것을 가정하고 표준화
        roleName = roleName.toUpperCase();

        if (roleRepository.findByRoleName(roleName).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 역할입니다: " + roleName);
        }

        Role role = new Role();
        role.setRoleName(roleName);
        return roleRepository.save(role);
    }

    @Transactional
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ROLE_ID: " + roleId));

        String roleName = role.getRoleName();
        if (roleName != null) {
            String upper = roleName.toUpperCase();
            // 안전장치: 시스템 기본 역할은 삭제 금지
            if ("ADMIN".equals(upper) || "USER".equals(upper)) {
                throw new IllegalArgumentException("기본 역할(" + upper + ")은 삭제할 수 없습니다.");
            }
        }

        if (memberRoleRepository.existsByRole_RoleId(roleId)) {
            throw new IllegalArgumentException("해당 역할을 가진 회원이 있어 삭제할 수 없습니다. (먼저 역할을 회수하세요)");
        }

        roleRepository.delete(role);
    }

    public List<RoleAdminRow> rows() {
        // ✅ 삭제된 회원 제외
        List<Member> members = memberRepository.findByStatusNot(com.carproject.member.entity.MemberStatus.DELETED);

        List<RoleAdminRow> rows = new ArrayList<>();
        for (Member m : members) {
            List<MemberRole> mrs = memberRoleRepository.findByMemberIdWithRole(m.getMemberId());
            Set<Long> assigned = mrs.stream().map(mr -> mr.getRole().getRoleId())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            String roleNames = mrs.stream().map(mr -> mr.getRole().getRoleName())
                    .distinct().collect(Collectors.joining(", "));
            rows.add(new RoleAdminRow(m.getMemberId(), m.getLoginId(), m.getName(), roleNames, assigned));
        }
        return rows;
    }


    @Transactional
    public void assignRoles(Long memberId, List<Long> roleIds) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 선택된 역할이 없다면: 모든 역할 제거
        memberRoleRepository.deleteByMember_MemberId(memberId);

        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }

        // 새로 연결
        for (Long roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ROLE_ID: " + roleId));
            memberRoleRepository.save(MemberRole.link(member, role));
        }
    }
}
