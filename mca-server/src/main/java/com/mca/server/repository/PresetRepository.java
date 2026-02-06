package com.mca.server.repository;

import com.mca.server.entity.Preset;
import org.springframework.data.jpa.repository.EntityGraph;
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

    Optional<Preset> findByWidgetToken(String widgetToken);

    /**
     * 使用 EntityGraph 预加载 anchors 和 anchor 实体
     */
    @EntityGraph(attributePaths = {"anchors", "anchors.anchor"})
    List<Preset> findWithAnchorsByDeviceIdAndUserId(String deviceId, String userId);

    /**
     * 使用 EntityGraph 预加载 anchors 和 anchor 实体
     */
    @EntityGraph(attributePaths = {"anchors", "anchors.anchor"})
    Optional<Preset> findWithAnchorsById(String presetId);

    /**
     * 使用 EntityGraph 预加载 anchors 和 anchor 实体
     */
    @EntityGraph(attributePaths = {"anchors", "anchors.anchor"})
    Optional<Preset> findWithAnchorsByWidgetToken(String widgetToken);
}
