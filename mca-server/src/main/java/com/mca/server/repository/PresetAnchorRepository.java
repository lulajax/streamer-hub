package com.mca.server.repository;

import com.mca.server.entity.PresetAnchor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PresetAnchorRepository extends JpaRepository<PresetAnchor, String> {

    List<PresetAnchor> findByPresetIdOrderByDisplayOrderAsc(String presetId);

    Optional<PresetAnchor> findByPresetIdAndAnchorId(String presetId, String anchorId);

    long countByAnchorId(String anchorId);
}
