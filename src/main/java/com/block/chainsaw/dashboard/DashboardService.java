package com.block.chainsaw.dashboard;

import com.block.chainsaw.contract.CompletedContractDTO;
import com.block.chainsaw.contract.ContractEntity;
import com.block.chainsaw.contract.ContractRepository;
import com.block.chainsaw.contract.subscription.ContractSubscription;
import com.block.chainsaw.contract.subscription.ContractSubscriptionRepository;
import com.block.chainsaw.model.ContractType;
import com.block.chainsaw.user.Entity.UserEntity;
import com.block.chainsaw.user.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final ContractSubscriptionRepository subscriptionRepository;

    // IPFS 게이트웨이 주소
    private static final String IPFS_GATEWAY = "https://gateway.pinata.cloud/ipfs/";

    @Transactional(readOnly = true)
    public DashboardDTO getDashboardData(String email) {

        // 유저 확인
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        // [내 계약] (내가 만든 것 전부)
        List<ContractEntity> myContractsEntity = contractRepository.findByCreatorOrderByCreatedAtDesc(user);
        List<CompletedContractDTO> myContracts = convertToDtoList(myContractsEntity);



        // 내가 구독한 기업 리스트 추출
        List<ContractSubscription> subs = subscriptionRepository.findBySubscriber(user);

        // 내가 구독한 기업들 (UserEntity 리스트)
        List<UserEntity> subscribedCreators = subs.stream()
                .map(ContractSubscription::getTarget)
                .collect(Collectors.toList());



        // 내가 구독한 기업의 CORPORATE 계약
        List<ContractEntity> subscribedEntity;
        if (subscribedCreators.isEmpty()) {
            subscribedEntity = Collections.emptyList();
        } else {
            subscribedEntity = contractRepository.findByCreatorInAndTypeOrderByCreatedAtDesc(
                    subscribedCreators, ContractType.CORPORATE);
        }
        List<CompletedContractDTO> subscribedTerms = convertToDtoList(subscribedEntity);

        List<ContractEntity> browseEntity;

        if (subscribedCreators.isEmpty()) {
            // 구독한 게 없으면 -> 모든 기업 약관 조회
            browseEntity = contractRepository.findByTypeOrderByCreatedAtDesc(ContractType.CORPORATE);
        } else {
            // 구독한 게 있으면 -> 그들만 빼고 조회 (NotIn)
            browseEntity = contractRepository.findByCreatorNotInAndTypeOrderByCreatedAtDesc(
                    subscribedCreators, ContractType.CORPORATE);
        }
        List<CompletedContractDTO> browseTerms = convertToDtoList(browseEntity);


        // 4. 최종 반환
        return DashboardDTO.builder()
                .myContracts(myContracts)
                .subscribedTerms(subscribedTerms)
                .browseTerms(browseTerms)
                .build();
    }

    // Entity List -> DTO List 변환
    private List<CompletedContractDTO> convertToDtoList(List<ContractEntity> entities) {
        return entities.stream()
                .map(c -> CompletedContractDTO.builder()
                        .contractId(c.getId())
                        .title(c.getTitle())
                        .type(c.getType())
                        .fileName(c.getFileName())
                        .fileUrl(IPFS_GATEWAY + c.getIpfsCid())
                        .tsHash(c.getTsHash())
                        .creator(c.getCreator().getName()) // 생성자(기업명)
                        .creatorEmail(c.getCreator().getEmail())
                        .createdAt(c.getCreatedAt())
                        .previousContractId(c.getPreviousContractId())
                        .build())
                .collect(Collectors.toList());
    }
}
