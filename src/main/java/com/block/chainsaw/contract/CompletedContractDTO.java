package com.block.chainsaw.contract;

import com.block.chainsaw.model.ContractType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CompletedContractDTO {
    private Long contractId;      // 계약 ID (DB PK)
    private String title;         // 제목
    private ContractType type;    // PERSONAL or CORPORATE
    private String fileName;      // 파일명
    private String fileUrl;       // 클릭하면 열리는 IPFS 링크
    private String tsHash;        // 블록체인 거래 영수증
    private String creator;       // 참여자/기업명 정보
    private String creatorEmail;  // 만든 사람 이메일
    private LocalDateTime creationDate;
    private LocalDateTime createdAt; // 등록일
    private String previousContractId; // 이전 버전 ID (없으면 null)
}