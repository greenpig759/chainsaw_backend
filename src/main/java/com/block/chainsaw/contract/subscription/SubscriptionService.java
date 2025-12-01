package com.block.chainsaw.contract.subscription;

import com.block.chainsaw.user.Entity.UserEntity;
import com.block.chainsaw.user.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final UserRepository userRepository;
    private final ContractSubscriptionRepository contractSubscriptionRepository;

    // 구독 로직
    @Transactional
    public void subcribe(String subcriberEmail, String targetEmail){
        // 구독자 조회[본인]
        UserEntity subscriber = userRepository.findByEmail(subcriberEmail)
                .orElseThrow(() -> new RuntimeException("내 정보를 찾을 수 없습니다"));

        // 기업 조회
        UserEntity target = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new RuntimeException("구독할 대상을 찾을 수 없습니다"));

        // 3. 유효성 검사: 자기 자신 구독 불가
        if (subscriber.getEmail().equals(target.getEmail())) {
            throw new RuntimeException("자기 자신은 구독할 수 없습니다.");
        }

        // 4. 유효성 검사: 이미 구독했는지 확인
        if (contractSubscriptionRepository.existsBySubscriberAndTarget(subscriber, target)) {
            throw new RuntimeException("이미 구독 중인 대상입니다.");
        }

        // 5. 저장
        ContractSubscription subscription = ContractSubscription.builder()
                .subscriber(subscriber)
                .target(target)
                .build();

        contractSubscriptionRepository.save(subscription);
    }
}
