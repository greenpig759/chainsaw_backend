package com.block.chainsaw.contract;

import com.block.chainsaw.login.privateKey.PrivateKeyEntity;
import com.block.chainsaw.login.privateKey.PrivateKeyRepository;
import com.block.chainsaw.user.Entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class WalletSecurityService {

    private final PrivateKeyRepository privateKeyRepository;

    // (나중에 사용할 암호화 설정)
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    private SecretKey masterKey;

    public WalletSecurityService(
            PrivateKeyRepository privateKeyRepository,
            @Value("${wallet.secret-key}") String secretKeyString) {
        this.privateKeyRepository = privateKeyRepository;
        byte[] keyBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
        this.masterKey = new SecretKeySpec(keyBytes, "AES");
    }

    // 1. [저장] 개인 키를 DB에 저장
    @Transactional
    public void saveEncryptedKey(UserEntity user, String rawPrivateKey) {
        try {
            // ==========================================
            //  (나중에 주석 해제하여 사용)
            // ==========================================
            /*
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, masterKey, spec);
            
            byte[] encryptedBytes = cipher.doFinal(rawPrivateKey.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

            String finalString = Base64.getEncoder().encodeToString(combined);
            
            PrivateKeyEntity secret = new PrivateKeyEntity(user, finalString, ALGORITHM);
            privateKeyRepository.save(secret);
            */


            // 과제의 결과 확인을 편하게 하기 위해 암호화를 적용하지 않고 수행
            // "PLAIN_" 접두사를 붙여서 나중에 암호화된 건지 아닌지 구별하기 쉽게 함
            String plainString = "PLAIN_" + rawPrivateKey;

            PrivateKeyEntity secret = new PrivateKeyEntity(user, plainString, "NONE");
            privateKeyRepository.save(secret);
            System.out.println(" [주의] 개인 키가 암호화되지 않고 저장되었습니다!");

        } catch (Exception e) {
            throw new RuntimeException("개인 키 저장 실패", e);
        }
    }


    // 2. [복호화] DB에서 개인 키 원본으로 복구
    @Transactional(readOnly = true)
    public String getDecryptedKey(UserEntity user) {
        try {
            PrivateKeyEntity secret = privateKeyRepository.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("지갑 정보를 찾을 수 없습니다."));

            // ==========================================
            // (나중에 주석 해제하여 사용)
            // ==========================================
            /*
            byte[] combined = Base64.getDecoder().decode(secret.getEncryptedPrivateKey());

            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);

            byte[] encryptedBytes = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, iv.length, encryptedBytes, 0, encryptedBytes.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, masterKey, spec);
            
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return new String(decryptedBytes, StandardCharsets.UTF_8);
            */

            // ==========================================
            // (평문 그대로 반환)
            // ==========================================
            String storedKey = secret.getPrivateKey();
            if (storedKey.startsWith("PLAIN_")) {
                return storedKey.substring(6); // "PLAIN_" 제거하고 반환
            } else {
                throw new RuntimeException("암호화된 데이터입니다. 개발 모드에서는 읽을 수 없습니다.");
            }

        } catch (Exception e) {
            throw new RuntimeException("개인 키 불러오기 실패", e);
        }
    }
}