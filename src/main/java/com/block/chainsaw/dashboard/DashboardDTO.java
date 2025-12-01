package com.block.chainsaw.dashboard;

import com.block.chainsaw.contract.CompletedContractDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardDTO { // 대쉬보드의 정보를 주는 DTO
    // 내 계약
    private List<CompletedContractDTO> myContracts;

    // 구독한 약관
    private List<CompletedContractDTO> subscribedTerms;

    // 약관 살펴보기
    private List<CompletedContractDTO> browseTerms;
}
