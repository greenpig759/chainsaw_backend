package com.block.chainsaw.contract.subscription;

import com.block.chainsaw.user.Entity.UserEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "contract_subscriptions")
public class ContractSubscription { // 기업의 약관 구독을 위한 Entity
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcriber_id", nullable = false)
    private UserEntity subscriber;

    // 구독 대상 (기업의 이메일 또는 유저 정보)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private UserEntity target;

    @Builder
    public ContractSubscription(UserEntity subscriber, UserEntity target) {
        this.subscriber = subscriber;
        this.target = target;
    }
}
