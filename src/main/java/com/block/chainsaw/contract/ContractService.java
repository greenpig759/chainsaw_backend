package com.block.chainsaw.contract;


import com.block.chainsaw.contract.IPFS.IpfsDTO;
import com.block.chainsaw.contract.IPFS.IpfsService;
import com.block.chainsaw.model.ContractType;
import com.block.chainsaw.user.Entity.UserEntity;
import com.block.chainsaw.user.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final IpfsService ipfsService;
    private final WalletSecurityService walletSecurityService;
    private final BlockchainService blockchainService;

    private static final String IPFS_GATEWAY = "https://gateway.pinata.cloud/ipfs/";

    @Transactional
    public ContractEntity registerContract(String email, MultipartFile file, ContractRegisterDTO dto) {

        // 1. 유저 조회
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // 2. Stirng -> Enum 변환
        ContractType type;
        try{
            type = ContractType.valueOf(dto.getType());
        }catch(IllegalArgumentException e){
            throw new RuntimeException("잘못된 계약 유형입니다 " + dto.getType());
        }

        // 3. 등록 실행
        return processRegistration(user, file, dto, type);
    }


    // 등록 실행
    private ContractEntity processRegistration(UserEntity user, MultipartFile file, ContractRegisterDTO dto, ContractType type) {

        // 1. 파일 IPFS 업로드 -> CID 획득
        System.out.println("1. IPFS 업로드 시작...");
        IpfsDTO ipfsResult = ipfsService.uploadFile(file);
        String cid = ipfsResult.fileHash();

        // 2. 개인 키 복호화
        System.out.println("2. 개인 키 복호화...");
        String privateKey = walletSecurityService.getDecryptedKey(user);

        // 3. 블록체인 트랜잭션 전송
        System.out.println("3. 블록체인 트랜잭션 전송...");
        String tsHash = blockchainService.registerContractOnChain(
                privateKey,
                type,
                cid,
                dto.getTitle(),
                user.getName(), // 기업명(참여자 정보)
                dto.getPreviousContractId() // 이전 계약 ID
        );
        System.out.println("트랜잭션 성공! TxHash: " + tsHash);

        // 4. DB 최종 저장
        System.out.println("4. DB 저장...");
        ContractEntity contract = ContractEntity.builder()
                .type(type)
                .creator(user)
                .title(dto.getTitle())
                .fileName(ipfsResult.fileName())
                .ipfsCid(cid)
                .tsHash(tsHash)
                .previousContractId(dto.getPreviousContractId())
                .createdAt(LocalDateTime.now())
                .build();

        return contractRepository.save(contract);
    }

    @Transactional
    public List<String> getParticipantsByCid(String cid) {
        // 같은 CID를 가진 계약들을 모두 찾아서 작성자의 이름만 준다
        return contractRepository.findByPreviousContractId(cid).stream()
                .map(c -> c.getCreator().getName())
                .distinct() // 중복제거
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompletedContractDTO> getContractHistory(Long contractId){
        List<ContractEntity> history = new ArrayList<>();

        // 1. 기준 계약 찾기
        ContractEntity current = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("계약 없음"));
        history.add(current);

        // 2. [과거 추적] (조상 찾기)
        ContractEntity temp = current;
        while (temp.getPreviousContractId() != null) {
            String prevHash = temp.getPreviousContractId();
            temp = contractRepository.findByTsHash(prevHash).orElse(null);

            if (temp == null) break;
            history.add(0, temp); // 리스트 맨 앞에 추가
        }

        // 3. [미래 추적] (자식 찾기)
        temp = current;
        while (true) {
            // ⭐️ 수정: "나의 해시(temp.getTxHash())"를 부모로 섬기는 계약을 찾아야 자식입니다.
            Optional<ContractEntity> nextOpt = contractRepository.findByPreviousContractId(temp.getTsHash());

            if (nextOpt.isEmpty()) break;

            temp = nextOpt.get();
            history.add(temp); // 리스트 뒤에 추가
        }

        // 4. DTO 변환
        return history.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Entity -> DTO 변환
    private CompletedContractDTO convertToDto(ContractEntity c) {
        return CompletedContractDTO.builder()
                .contractId(c.getId())
                .title(c.getTitle())
                .type(c.getType())
                .fileName(c.getFileName())
                .fileUrl(IPFS_GATEWAY + c.getIpfsCid()) //  클릭 가능한 링크 생성
                .tsHash(c.getTsHash())
                .creator(c.getCreator().getName())      // 생성자 이름 (또는 이메일)
                .createdAt(c.getCreatedAt())
                .previousContractId(c.getPreviousContractId())
                .build();
    }
}