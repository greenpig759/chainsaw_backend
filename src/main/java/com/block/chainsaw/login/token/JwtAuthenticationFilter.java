package com.block.chainsaw.login.token;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 요청 도착 확인
        System.out.println("=== [JwtFilter] 요청 도착: " + request.getMethod() + " " + request.getRequestURI());

        try {
            // 1. 요청 헤더에서 토큰 추출
            String jwt = getJwtFromRequest(request);

            // 토큰 추출 결과 확인
            if (jwt == null) {
                System.out.println("--- [JwtFilter] 토큰 없음 (헤더가 없거나 Bearer 형식이 아님)");
            } else {
                System.out.println("--- [JwtFilter] 토큰 발견: " + jwt.substring(0, 10) + "...");
            }

            // 2. 토큰이 존재하고, 유효성 검증(getClaims)을 통과했는지 확인
            if (StringUtils.hasText(jwt)) {
                Claims claims = jwtTokenProvider.getClaims(jwt);

                if (claims != null) {
                    String email = claims.getSubject();

                    // 검증 성공
                    System.out.println("--- [JwtFilter] 토큰 검증 성공! 사용자: " + email);

                    // 3. 인증 객체 생성
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(email, null, new ArrayList<>());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 4. 인증 등록
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    // 검증 실패 (getClaims가 null 반환)
                    System.out.println("--- [JwtFilter] 토큰 검증 실패 (유효하지 않음)");
                }
            }
        } catch (Exception e) {
            // 예외 발생
            System.err.println("!!! [JwtFilter] 에러 발생: " + e.getMessage());
            e.printStackTrace(); // 상세 에러 내용 출력
        }

        // 5. 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    // Authorization 헤더에서 'Bearer '를 제거하고 토큰만 추출
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // 헤더 값 확인
        System.out.println("--- [JwtFilter] Authorization 헤더 값: " + bearerToken);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}