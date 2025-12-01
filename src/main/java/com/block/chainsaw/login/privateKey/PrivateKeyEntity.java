package com.block.chainsaw.login.privateKey;

import com.block.chainsaw.user.Entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "private_keys")
public class PrivateKeyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user와 1:1
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false, length = 512)
    private String privateKey;

    // 암호화 알고리즘
    private String algorithm;

    public PrivateKeyEntity(UserEntity user, String privateKey, String algorithm) {
        this.user = user;
        this.privateKey = privateKey;
        this.algorithm = algorithm;
    }
}
