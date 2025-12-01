package com.block.chainsaw.contract;

import com.block.chainsaw.model.ContractType;
import com.block.chainsaw.user.Entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "contracts")
public class ContractEntity { // 기업의 공지 약관 Entity
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1. 계약 유형(personal / corporate)
    @Enumerated(EnumType.STRING)
    private ContractType type;

    // 2. 등록한 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity creator;

    // 3. 메타데이터(제목, 원본 파일명)
    @Column(nullable = false)
    private String title;
    private String fileName;

    // 4. 파일 정보(CID, 블록체인 거래 증거)
    @Column(nullable = false)
    private String ipfsCid;

    @Column(nullable = false)
    private String tsHash;

    // 5. 이전 계약 ID
    private String previousContractId;

    // 6. 생성 시간(DB 기준)
    private LocalDateTime createdAt;
}
