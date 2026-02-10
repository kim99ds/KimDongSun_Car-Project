package com.carproject.car.service;

import com.carproject.car.dto.LikeStatusDto;
import com.carproject.car.entity.CarModel;
import com.carproject.car.entity.ModelLike;
import com.carproject.car.entity.ModelLikeId;
import com.carproject.car.repository.CarModelRepository;
import com.carproject.car.repository.ModelLikeRepository;
import com.carproject.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ModelLikeService {

    private final ModelLikeRepository modelLikeRepository;
    private final CarModelRepository carModelRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public LikeStatusDto status(Long modelId, Long memberId) {
        boolean liked = modelLikeRepository.existsById(new ModelLikeId(modelId, memberId));
        Long likeCount = carModelRepository.findById(modelId)
                .map(m -> m.getLikeCount())
                .orElse(0L);
        return new LikeStatusDto(modelId, memberId, liked, likeCount);
    }

    @Transactional
    public LikeStatusDto like(Long modelId, Long memberId) {
        ModelLikeId id = new ModelLikeId(modelId, memberId);
        if (!modelLikeRepository.existsById(id)) {
            ModelLike ml = new ModelLike();
            ml.setId(id);
            ml.setModel(carModelRepository.getReferenceById(modelId));
            ml.setMember(memberRepository.getReferenceById(memberId));
            modelLikeRepository.save(ml);
        }

        // DB 트리거가 LIKE_COUNT를 올린다.
        Long likeCount = carModelRepository.findById(modelId)
                .map(m -> m.getLikeCount())
                .orElse(0L);
        return new LikeStatusDto(modelId, memberId, true, likeCount);
    }

    @Transactional
    public LikeStatusDto unlike(Long modelId, Long memberId) {
        ModelLikeId id = new ModelLikeId(modelId, memberId);
        modelLikeRepository.deleteById(id);

        // DB 트리거가 LIKE_COUNT를 내린다.
        Long likeCount = carModelRepository.findById(modelId)
                .map(m -> m.getLikeCount())
                .orElse(0L);
        return new LikeStatusDto(modelId, memberId, false, likeCount);
    }

    public Long likeCount(Long modelId) {
        // 예시: CarModelRepository에서 likeCount 조회
        return carModelRepository.findById(modelId)
                .map(CarModel::getLikeCount)
                .orElse(0L);
    }

}
