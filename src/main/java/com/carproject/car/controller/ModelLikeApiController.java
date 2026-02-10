package com.carproject.car.controller;

import com.carproject.car.dto.LikeStatusDto;
import com.carproject.car.service.ModelLikeService;
import com.carproject.global.security.SecurityMemberResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ModelLikeApiController {

    private final ModelLikeService modelLikeService;
    private final SecurityMemberResolver securityMemberResolver;

    /**
     * ✅ 정책 B
     * - GET: 비로그인 허용 (liked=false, memberId=null, likeCount만 내려줌)
     * - POST/DELETE: 로그인 필수(401)
     */
    @GetMapping("/api/models/{modelId}/likes")
    public LikeStatusDto status(@PathVariable Long modelId, Authentication authentication) {
        Long memberId = securityMemberResolver.resolveMemberIdOrNull(authentication);

        if (memberId == null) {
            Long likeCount = modelLikeService.likeCount(modelId);
            return new LikeStatusDto(modelId, null, false, likeCount);
        }

        return modelLikeService.status(modelId, memberId);
    }

    @PostMapping("/api/models/{modelId}/likes")
    public LikeStatusDto like(@PathVariable Long modelId, Authentication authentication) {
        Long memberId = securityMemberResolver.requireMemberId(authentication);
        return modelLikeService.like(modelId, memberId);
    }

    @DeleteMapping("/api/models/{modelId}/likes")
    public LikeStatusDto unlike(@PathVariable Long modelId, Authentication authentication) {
        Long memberId = securityMemberResolver.requireMemberId(authentication);
        return modelLikeService.unlike(modelId, memberId);
    }
}
