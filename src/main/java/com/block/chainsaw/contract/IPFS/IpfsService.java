package com.block.chainsaw.contract.IPFS;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class IpfsService {

    // yml에서 설정값 가져오기
    @Value("${pinata.base-url}")
    private String pinataUrl;

    @Value("${pinata.jwt}") // (JWT 방식을 권장)
    private String pinataJwt;

    /**
     * ⭐️ 파일을 Pinata(IPFS)에 업로드하고 실제 CID를 반환
     */
    public IpfsDTO uploadFile(MultipartFile file) {
        try {
            // 1. WebClient 생성 (HTTP 요청 도구)
            WebClient webClient = WebClient.builder()
                    .baseUrl(pinataUrl)
                    .defaultHeader("Authorization", "Bearer " + pinataJwt) // 인증 헤더
                    .build();

            // 2. Multipart Body 구성 (파일 담기)
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename(); // 파일명 필수
                }
            });

            // 3. Pinata API 호출 (POST /pinning/pinFileToIPFS)
            JsonNode response = webClient.post()
                    .uri("/pinning/pinFileToIPFS")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(JsonNode.class) // 응답을 JSON으로 받음
                    .block(); // 결과가 올 때까지 기다리는 동기 처리

            // 4. 결과 파싱 (Pinata가 준 실제 CID)
            String ipfsHash = response.get("IpfsHash").asText();
            long fileSize = response.get("PinSize").asLong();

            // 5. 결과 반환
            String viewUrl = "https://gateway.pinata.cloud/ipfs/" + ipfsHash;

            return new IpfsDTO(
                    viewUrl,            // 브라우저에서 볼 수 있는 주소
                    ipfsHash,           // 블록체인에 저장될 CID
                    file.getOriginalFilename(),
                    fileSize
            );

        } catch (IOException e) {
            throw new RuntimeException("IPFS 업로드 실패 (파일 읽기 오류)", e);
        } catch (Exception e) {
            throw new RuntimeException("IPFS 업로드 실패 (Pinata API 오류)", e);
        }
    }
}