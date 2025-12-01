package com.block.chainsaw.login;

import com.block.chainsaw.login.privateKey.PrivateKeyEntity;
import com.block.chainsaw.login.privateKey.PrivateKeyRepository;
import com.block.chainsaw.login.privateKey.WalletService;
import com.block.chainsaw.login.token.JwtTokenProvider;
import com.block.chainsaw.login.token.RefreshToken;
import com.block.chainsaw.login.token.RefreshTokenRepository;
import com.block.chainsaw.user.Entity.UserEntity;
import com.block.chainsaw.user.Repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component // 클래스를 스프링 Bean으로 등록
@RequiredArgsConstructor // final 필드들을 위한 생성자를 자동으로 만든다
public class Oauth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PrivateKeyRepository privateKeyRepository;

    @Override // 부모 클래스의 메서드를 덮어쓴다
    @Transactional // 오류 시 모든 DB 작업을 취소
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication
    )throws IOException, ServletException {
        // request - Http 요청 객체
        // response - Http 응답
        // outhentication - 인증 객체(구글에서 발급한 신원 보증서)
        // IOException - 입출력, 네트워크나 파일 시스템과의 통신 중 문제 생겼을 때
        // ServletException - 자바 서블릿 내부에서 문제가 생겼을 때

        // 신원 보증서에서 사용자 정보 추출
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // 이메일을 사용해 DB에서 유저 조회, 없다면 신규 생성
        UserEntity user = userRepository.findByEmail(email).orElseGet(() -> {
            // 신규 유저 생성 로직
            System.out.println("신규 유저입니다 지갑을 생성합니다");
            WalletService.WalletData newWallet = walletService.createWallet();

            // 신규 유저 생성 로직
            UserEntity newUser = userRepository.save(UserEntity
                    .builder()
                    .email(email)
                    .name(name)
                    .walletAddress(newWallet.address())
                    .build());

            // 개인키 생성
            privateKeyRepository.save(PrivateKeyEntity
                    .builder()
                    .user(newUser)
                    .privateKey(newWallet.privateKey())
                    .build());

            // 저장된 newUser 객체 반환
            return newUser;
        });

        // JWT 토큰 2개 발급
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        // 리프레시 토큰을 DB에 저장
        RefreshToken newRefreshToken = new RefreshToken(user, refreshToken);
        refreshTokenRepository.save(newRefreshToken);

        // 프론트엔드로 보낼 URL을 조립
        String targetUrl = UriComponentsBuilder
                .fromUriString("http://localhost:3000/login-success.html")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        // 사용자의 브라우저를 위에서 만든 URL로 리디렉션 시킴
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
