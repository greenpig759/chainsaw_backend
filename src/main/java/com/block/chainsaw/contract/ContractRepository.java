package com.block.chainsaw.contract;

import com.block.chainsaw.model.ContractType;
import com.block.chainsaw.user.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<ContractEntity, Long> {
    // 1. [내 계약] 내가 만든 모든 계약 (최신순)
    List<ContractEntity> findByCreatorOrderByCreatedAtDesc(UserEntity creator);

    // 2. [구독한 약관] 특정 생성자들(구독한 기업들)이 만든 특정 타입(CORPORATE) 계약
    List<ContractEntity> findByCreatorInAndTypeOrderByCreatedAtDesc(List<UserEntity> creators, ContractType type);

    // 3. [약관 살펴보기] 특정 생성자들(구독한 기업들)을 "제외한" 특정 타입(CORPORATE) 계약
    List<ContractEntity> findByCreatorNotInAndTypeOrderByCreatedAtDesc(List<UserEntity> creators, ContractType type);

    // 4. [약관 살펴보기 (구독 없을 때)] 모든 기업 약관
    List<ContractEntity> findByTypeOrderByCreatedAtDesc(ContractType type);

    // 5. 같은 CID를 가진 모든 계약 찾기
    List<ContractEntity> findByIpfsCid(String ipfsCid);

    // 6. 연관 계약 찾기
    Optional<ContractEntity> findByPreviousContractId(String previousContractId);

    Optional<ContractEntity> findByTsHash(String tsHash);
}
