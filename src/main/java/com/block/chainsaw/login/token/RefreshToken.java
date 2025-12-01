package com.block.chainsaw.login.token;

import com.block.chainsaw.user.Entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", nullable = false)
    private UserEntity user;

    @Column(nullable = false, length = 512, unique = true)
    private String token;

    // 생성자
    public RefreshToken(UserEntity user, String token) {
        this.user = user;
        this.token = token;
    }
}
