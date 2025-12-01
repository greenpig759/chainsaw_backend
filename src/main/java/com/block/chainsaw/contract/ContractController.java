package com.block.chainsaw.contract;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {
    private final ContractService contractService;

    // 계약 등록 API
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerContract(
            @AuthenticationPrincipal String email, // 토큰에서 추출한 이메일
            @RequestPart("file")MultipartFile file, // 파일 받기
            @ModelAttribute ContractRegisterDTO dto // 나머지 정보
            ){
        System.out.println("=== [ContractController] 요청 수신 성공 ===");
        System.out.println("요청자: " + email);
        System.out.println("파일명: " + file.getOriginalFilename());
        System.out.println("유형: " + dto.getType());

        // IPFS 저장 -> 블록체인 기록 -> DB 저장
        ContractEntity savedContract = contractService.registerContract(email, file, dto);

        // 결과 반환 (프론트엔드 alert창에 띄워줄 tsHash 포함)
        Map<String, Object> response = new HashMap<>();
        response.put("message", "등록 성공");
        response.put("contractId", savedContract.getId());
        response.put("tsHash", savedContract.getTsHash());

        return ResponseEntity.ok(response);
    }

    // ContractController.java

    // 1. 참여자 확인 API
    @GetMapping("/participants")
    public ResponseEntity<List<String>> getParticipants(@RequestParam("cid") String cid) {
        return ResponseEntity.ok(contractService.getParticipantsByCid(cid));
    }

    // 2. 히스토리(연관 계약) 확인 API
    @GetMapping("/{contractId}/history")
    public ResponseEntity<List<CompletedContractDTO>> getHistory(@PathVariable Long contractId) {
        return ResponseEntity.ok(contractService.getContractHistory(contractId));
    }
    
}
