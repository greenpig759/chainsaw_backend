package com.block.chainsaw.user.Repository;

import com.block.chainsaw.user.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // 이메일로 사용자를 찾는 기능
    Optional<UserEntity> findByEmail(String email);
}
