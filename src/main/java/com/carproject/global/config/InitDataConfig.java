package com.carproject.global.config;

import com.carproject.member.entity.*;
import com.carproject.member.repository.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class InitDataConfig {

    private final EntityManager em;

    @Bean
    @Transactional
    public org.springframework.boot.CommandLineRunner initData(
            RoleRepository roleRepository,
            MemberRepository memberRepository,
            MemberRoleRepository memberRoleRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            // 1) ROLE USER/ADMIN 없으면 생성
            Role user = roleRepository.findByRoleName("USER").orElseGet(() -> {
                Role r = new Role();
                r.setRoleId(fetchNextSeq("SEQ_ROLE"));   // ✅ setId -> setRoleId
                r.setRoleName("USER");
                return roleRepository.save(r);
            });

            Role admin = roleRepository.findByRoleName("ADMIN").orElseGet(() -> {
                Role r = new Role();
                r.setRoleId(fetchNextSeq("SEQ_ROLE"));   // ✅ setId -> setRoleId
                r.setRoleName("ADMIN");
                return roleRepository.save(r);

            });

            // 2) 관리자 계정 없으면 생성
            Member adminMember = memberRepository.findByLoginId("admin").orElse(null);
            if (adminMember == null) {
                Member m = new Member();
                m.setMemberId(fetchNextSeq("SEQ_MEMBER")); // ✅ setId -> setMemberId
                m.setLoginId("admin");
                m.setPassword(passwordEncoder.encode("1234"));
                m.setName("관리자");
                m.setEmail("admin@car.com");
                m.setBirthDate(LocalDate.of(1990, 1, 1));
                m.setStatus(MemberStatus.ACTIVE);
                adminMember = memberRepository.save(m);

            }

            // 3) ADMIN 권한 없으면 연결
            boolean hasAdmin = memberRoleRepository.existsByMember_MemberIdAndRole_RoleName(adminMember.getId(), "ADMIN");
            if (!hasAdmin) {
                memberRoleRepository.save(MemberRole.link(adminMember, admin));
            }
        };
    }

    private Long fetchNextSeq(String seqName) {
        Object v = em.createNativeQuery("SELECT " + seqName + ".NEXTVAL FROM DUAL")
                .getSingleResult();
        return ((Number) v).longValue();
    }
}
