package com.block.chainsaw.contract;

import com.block.chainsaw.model.ContractType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import jakarta.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class BlockchainService {

    @Value("${blockchain.rpc-url}")
    private String rpcUrl;

    @Value("${blockchain.contract-address}")
    private String contractAddress;

    @Value("${blockchain.chain-id}")
    private long chainId;

    private Web3j web3j;

    // 1. 서버 시작 시 Web3j 연결 초기화
    @PostConstruct
    public void init() {
        this.web3j = Web3j.build(new HttpService(rpcUrl));
    }

    public String registerContractOnChain(
            String privateKey,      // 사용자 개인 키 (서명용)
            ContractType type,      // 계약 유형
            String ipfsCid,         // 파일 해시
            String title,           // 제목
            String parties,         // 참여자 정보
            String previousId         // 이전 계약 ID (없으면 0)
    ) {
        try {
            // 2. 자격 증명 로드 (개인 키로 지갑 열기)
            Credentials credentials = Credentials.create(privateKey);

            // 3. 호출할 함수 정의 (Solidity 함수명과 파라미터 타입 일치시켜야 함)
            // function registerContract(uint8, string, string, string, uint256)
            Function function = new Function(
                    "registerContract",
                    Arrays.asList(
                            new Uint8(type == ContractType.CORPORATE ? 1 : 0), // Enum -> 숫자 변환
                            new Utf8String(ipfsCid),
                            new Utf8String(title),
                            new Utf8String(parties),
                            new Utf8String(previousId != null ? previousId : "")
                    ),
                    Collections.emptyList() // 리턴값은 트랜잭션이라 필요 없음
            );

            // 4. 함수 데이터를 16진수로 인코딩
            String encodedFunction = FunctionEncoder.encode(function);

            // 5. Nonce 조회 (거래 순서 번호)
            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                    credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();

            // 6. 트랜잭션 생성 (가스비 등 설정)
            BigInteger gasLimit = BigInteger.valueOf(3000000);
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
            //BigInteger gasPrice = BigInteger.ZERO;

            RawTransaction rawTransaction = RawTransaction.createTransaction(
                    nonce, gasPrice, gasLimit, contractAddress, encodedFunction
            );

            // 7. 서명 (Sign)
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
            String hexValue = Numeric.toHexString(signedMessage);

            // 8. 전송 (Send)
            EthSendTransaction transactionResponse = web3j.ethSendRawTransaction(hexValue).send();

            if (transactionResponse.hasError()) {
                throw new RuntimeException("블록체인 오류: " + transactionResponse.getError().getMessage());
            }

            // 9. 결과 반환 (TxHash)
            return transactionResponse.getTransactionHash();

        } catch (Exception e) {
            throw new RuntimeException("스마트 컨트랙트 호출 실패", e);
        }
    }
}
