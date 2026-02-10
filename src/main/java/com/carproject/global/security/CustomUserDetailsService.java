package com.carproject.global.security;

import com.carproject.member.entity.Member;
import com.carproject.member.entity.MemberStatus;
import com.carproject.member.repository.MemberRepository;
import com.carproject.member.repository.MemberRoleRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final MemberRoleRepository memberRoleRepository;

    public CustomUserDetailsService(MemberRepository memberRepository,
                                    MemberRoleRepository memberRoleRepository) {
        this.memberRepository = memberRepository;
        this.memberRoleRepository = memberRoleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("No member: " + loginId));

        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new DisabledException("Member is not ACTIVE: " + member.getStatus());
        }

        // ✅ role fetch join으로 가져와서 LazyInitializationException 방지
        var authorities = memberRoleRepository.findByMemberIdWithRole(member.getMemberId()).stream()
                .map(mr -> new SimpleGrantedAuthority("ROLE_" + mr.getRole().getRoleName()))
                .toList();

        return new org.springframework.security.core.userdetails.User(
                member.getLoginId(),
                member.getPassword(),
                authorities
        );
    }
}
