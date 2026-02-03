package com.mca.server.repository;

import com.mca.server.entity.Activation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActivationRepository extends JpaRepository<Activation, String> {
    
    Optional<Activation> findByActivationCode(String activationCode);
    
    Optional<Activation> findByDeviceId(String deviceId);
    
    boolean existsByActivationCode(String activationCode);
    
    boolean existsByDeviceId(String deviceId);
}
