package com.block.chainsaw.login.privateKey;

import org.springframework.stereotype.Service;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

import java.security.SecureRandom;


@Service
public class WalletService {
    // 지갑 주소와 개인키를 함께 반환
    public record WalletData(String address, String privateKey){}

    public WalletData createWallet(){
        try{
            // "개인키"와 "공개키" 한 쌍 생성
            ECKeyPair ecKeyPair = Keys.createEcKeyPair(new SecureRandom());

            // 개인키 16진수 문자열로 변환해서 추출
            String privateKey = ecKeyPair.getPrivateKey().toString(16);

            // 지갑 주소를 생성
            String address = "0x" + Keys.getAddress(ecKeyPair.getPublicKey());

            // 주소와 개인 키를 담아서 반환
            return new WalletData(address, privateKey);
        } catch (Exception e){
            throw new RuntimeException("지갑 생성 실패", e);
        }
    }
}
