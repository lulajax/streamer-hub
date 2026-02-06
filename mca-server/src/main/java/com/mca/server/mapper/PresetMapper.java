package com.mca.server.mapper;

import com.mca.server.dto.AnchorDTO;
import com.mca.server.dto.PresetDTO;
import com.mca.server.entity.Anchor;
import com.mca.server.entity.Preset;
import com.mca.server.entity.PresetAnchor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Comparator;
import java.util.List;

/**
 * Preset 实体与 DTO 映射器
 */
@Mapper(componentModel = "spring")
public interface PresetMapper {

    PresetMapper INSTANCE = Mappers.getMapper(PresetMapper.class);

    /**
     * 将 Preset 实体转换为 DTO
     * 注意：需要在事务中调用，确保 exclusiveGifts 已加载
     */
    @Mapping(source = "anchors", target = "anchors", qualifiedByName = "mapAnchors")
    PresetDTO toDto(Preset preset);

    /**
     * 将 PresetAnchor 列表转换为 AnchorDTO 列表
     */
    @Named("mapAnchors")
    default List<AnchorDTO> mapAnchors(List<PresetAnchor> presetAnchors) {
        if (presetAnchors == null) {
            return null;
        }
        return presetAnchors.stream()
                .sorted(Comparator.comparing(
                        PresetAnchor::getDisplayOrder,
                        Comparator.nullsLast(Integer::compareTo)
                ))
                .map(this::toAnchorDto)
                .toList();
    }

    /**
     * 将 PresetAnchor 转换为 AnchorDTO
     */
    @Mapping(source = "anchor.id", target = "id")
    @Mapping(source = "anchor.tiktokId", target = "tiktokId")
    @Mapping(source = "anchor.name", target = "name")
    @Mapping(source = "anchor.avatarUrl", target = "avatarUrl")
    @Mapping(source = "exclusiveGifts", target = "exclusiveGifts")
    @Mapping(source = "totalScore", target = "totalScore")
    @Mapping(source = "isEliminated", target = "isEliminated")
    @Mapping(source = "isActive", target = "isActive")
    @Mapping(source = "displayOrder", target = "displayOrder")
    AnchorDTO toAnchorDto(PresetAnchor presetAnchor);

    /**
     * 将 Anchor 实体转换为 DTO（不带 PresetAnchor 信息）
     */
    @Mapping(target = "exclusiveGifts", ignore = true)
    @Mapping(target = "totalScore", ignore = true)
    @Mapping(target = "isEliminated", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "displayOrder", ignore = true)
    AnchorDTO toDto(Anchor anchor);

    /**
     * 转换 Preset 列表
     */
    List<PresetDTO> toDtoList(List<Preset> presets);
}
