package com.mca.server.repository;

import com.mca.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByEmailVerificationToken(String token);
    
    Optional<User> findByActivationCode(String activationCode);
    
    Optional<User> findByPasswordResetToken(String token);
    
    Optional<User> findByDeviceId(String deviceId);
    
    boolean existsByEmail(String email);
    
    boolean existsByActivationCode(String activationCode);
}
