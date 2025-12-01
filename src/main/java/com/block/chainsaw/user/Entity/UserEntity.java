package com.block.chainsaw.user.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // 구글 로그인 한 유저의 이메일 들어간다
    @Column(nullable = false)
    private String email;

    // 사용자의 지갑주소
    @Column(nullable = false)
    private String walletAddress;
}
