package com.carproject.global.init;

import com.carproject.member.entity.Member;
import com.carproject.member.entity.MemberRole;
import com.carproject.member.entity.MemberStatus;
import com.carproject.member.entity.Role;
import com.carproject.member.repository.MemberRepository;
import com.carproject.member.repository.MemberRoleRepository;
import com.carproject.member.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final MemberRoleRepository memberRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional // 트랜잭션 안에서 Dirty Checking을 활용해 업데이트 수행
    public void run(String... args) {

        Role userRole  = getOrCreateRole("USER");
        Role adminRole = getOrCreateRole("ADMIN");

        /* =========================
           ADMIN (관리자)
           ========================= */
        // ✅ 수정 포인트: orElseGet 대신 findByLoginId로 먼저 찾은 후 분기 처리
        Member admin = memberRepository.findByLoginId("admin").orElse(null);

        if (admin == null) {
            // 1. 계정이 아예 없으면 새로 생성
            admin = new Member();
            admin.setLoginId("admin");
            admin.setPassword(passwordEncoder.encode("1234")); // 암호화 저장
            admin.setName("최고관리자");
            admin.setEmail("admin@carproject.com");
            admin.setBirthDate(LocalDate.of(1990, 1, 1));
            admin.setStatus(MemberStatus.ACTIVE);
            admin = memberRepository.save(admin);
        } else {
            // 2. ✅ 핵심 수정: 기존 계정(CSV 병합 데이터)이 있다면 비번/상태를 강제 업데이트
            // 하드코딩된 평문 "1234"를 암호화된 값으로 교체하여 로그인 가능하게 함
            admin.setPassword(passwordEncoder.encode("1234"));
            admin.setStatus(MemberStatus.ACTIVE);
            // @Transactional이 걸려있어 save() 호출 없이도 서버 종료 시 자동 DB 반영(Dirty Checking)
        }

        // 3. 권한 연결 (CSV 병합 시 MEMBER_ROLE 테이블이 비어있을 수 있으므로 재확인)
        if (!memberRoleRepository.existsByMember_MemberIdAndRole_RoleName(admin.getMemberId(), "ADMIN")) {
            memberRoleRepository.save(MemberRole.link(admin, adminRole));
        }

        /* =========================
           USER (테스트 유저)
           ========================= */
        Member user = memberRepository.findByLoginId("jjang051").orElse(null);

        if (user == null) {
            user = new Member();
            user.setLoginId("jjang051");
            user.setPassword(passwordEncoder.encode("1234"));
            user.setName("장성호");
            user.setEmail("jjang051@carproject.com");
            user.setBirthDate(LocalDate.of(1995, 5, 5));
            user.setStatus(MemberStatus.ACTIVE);
            user = memberRepository.save(user);
        } else {
            // 테스트 유저도 병합 데이터라면 비번을 암호화로 교체
            user.setPassword(passwordEncoder.encode("1234"));
            user.setStatus(MemberStatus.ACTIVE);
        }

        if (!memberRoleRepository.existsByMember_MemberIdAndRole_RoleName(user.getMemberId(), "USER")) {
            memberRoleRepository.save(MemberRole.link(user, userRole));
        }
    }

    private Role getOrCreateRole(String roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setRoleName(roleName);
                    return roleRepository.save(r);
                });
    }
}