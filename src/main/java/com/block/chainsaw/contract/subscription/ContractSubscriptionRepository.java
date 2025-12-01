package com.block.chainsaw.contract.subscription;

import com.block.chainsaw.user.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractSubscriptionRepository extends JpaRepository<ContractSubscription, Long> {
    // 내가 구독한 목록 가져오기
    List<ContractSubscription> findBySubscriber(UserEntity subscriber);

    // 이미 구독했는지 확인용
    boolean existsBySubscriberAndTarget(UserEntity subscriber, UserEntity target);
}
