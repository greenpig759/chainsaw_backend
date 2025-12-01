package com.block.chainsaw.login.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    // yml 파일의 설정값을 읽어와서 초기화
    public JwtTokenProvider(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.access-expiration-ms}") long accessExpirationMs,
            @Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs){
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    // 사용할 이메일과 지갑 주소를 받아 JWT를 생성한다(엑세스 토큰)
    public String generateAccessToken(String email){
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + accessExpirationMs);

        // JWT 생성(엑세스 토큰)
        return Jwts.builder()
                .subject(email) // 토큰 주체 설정
                .issuedAt(now) // 생성시간
                .expiration(expirationDate) // 만료 시간
                .signWith(key) // 서명
                .compact(); // 조립 완료
    }

    // JWT 생성(리프레시 토큰)
    public String generateRefreshToken(String email){
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + refreshExpirationMs);

        // JWT 생성
        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(key)
                .compact();
    }

    // 예외 처리 상세화 할 것
    // 토큰이 유효한지 검증 및 이메일 추출
    public Claims getClaims(String token){
        try{
            // yml에 있는 비밀키로 토큰을 검사, 성공하면 페이로드를 반환
            return Jwts.parser() // 해석기를 부름
                    .verifyWith(key) // 서명을 하기 위한 key값 설정
                    .build()// 해석기를 생성
                    .parseSignedClaims(token) // 서명, 만료, 페이로드 해석
                    .getPayload(); // 페이로드 부분을 꺼낸다
        }catch(Exception ex) {
            System.err.println("만료된 토큰");
            return null;
        }
    }
}
