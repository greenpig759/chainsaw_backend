package com.block.chainsaw.login.privateKey;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrivateKeyRepository extends JpaRepository<PrivateKeyEntity, Long> {
    Optional<PrivateKeyEntity> findById(Long id);
}
