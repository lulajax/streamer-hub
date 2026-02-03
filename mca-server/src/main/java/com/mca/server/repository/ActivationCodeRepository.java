package com.mca.server.repository;

import com.mca.server.entity.ActivationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActivationCodeRepository extends JpaRepository<ActivationCode, String> {
    
    Optional<ActivationCode> findByCode(String code);
    
    boolean existsByCode(String code);
}
