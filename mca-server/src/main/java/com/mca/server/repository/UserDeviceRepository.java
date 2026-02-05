package com.mca.server.repository;

import com.mca.server.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, String> {

    Optional<UserDevice> findByUserIdAndDeviceId(String userId, String deviceId);

    long countByUserId(String userId);
}
