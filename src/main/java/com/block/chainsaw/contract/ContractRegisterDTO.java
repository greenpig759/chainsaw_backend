package com.block.chainsaw.contract;

import com.block.chainsaw.model.ContractType;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ContractRegisterDTO { // 사용자가 입력한 계약 정보
    private String title; // 계약 제목
    private String type; // 계약 유형(개인, 약관)
    private String previousContractId; // 이전 계약 ID(없으면 null)
}
