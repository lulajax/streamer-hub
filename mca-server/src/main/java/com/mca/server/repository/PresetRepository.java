package com.mca.server.repository;

import com.mca.server.entity.Preset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PresetRepository extends JpaRepository<Preset, String> {

    List<Preset> findByDeviceId(String deviceId);

    long countByDeviceId(String deviceId);

    List<Preset> findByDeviceIdAndUserId(String deviceId, String userId);

    long countByDeviceIdAndUserId(String deviceId, String userId);

    List<Preset> findByDeviceIdAndIsDefaultTrue(String deviceId);

    List<Preset> findByDeviceIdAndUserIdAndIsDefaultTrue(String deviceId, String userId);

    Optional<Preset> findByWidgetToken(String widgetToken);

    List<Preset> findByGameMode(Preset.GameMode gameMode);

    List<Preset> findByIsDefaultTrue();

    Optional<Preset> findByName(String name);

    long countByGameMode(Preset.GameMode gameMode);
}
