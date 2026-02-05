package com.mca.server.service;

import com.mca.server.dto.UserAnchorDTO;
import com.mca.server.dto.request.CreateUserAnchorRequest;
import com.mca.server.dto.request.UpdateUserAnchorRequest;
import com.mca.server.entity.Anchor;
import com.mca.server.exception.BusinessException;
import com.mca.server.repository.AnchorRepository;
import com.mca.server.repository.PresetAnchorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnchorService {

    private final AnchorRepository anchorRepository;
    private final PresetAnchorRepository presetAnchorRepository;

    @Transactional(readOnly = true)
    public List<UserAnchorDTO> listUserAnchors(String userId) {
        return anchorRepository.findByUserId(userId).stream()
                .map(UserAnchorDTO::fromEntity)
                .toList();
    }

    @Transactional
    public UserAnchorDTO createUserAnchor(String userId, CreateUserAnchorRequest request) {
        String tiktokId = normalize(request.getTiktokId());
        if (tiktokId != null && anchorRepository.findByUserIdAndTiktokId(userId, tiktokId).isPresent()) {
            throw new BusinessException("主播已存在");
        }

        Anchor anchor = Anchor.builder()
                .userId(userId)
                .tiktokId(tiktokId)
                .name(request.getName())
                .avatarUrl(normalize(request.getAvatarUrl()))
                .build();

        Anchor saved = anchorRepository.save(anchor);
        log.info("User anchor created: {} for user: {}", saved.getId(), userId);
        return UserAnchorDTO.fromEntity(saved);
    }

    @Transactional
    public UserAnchorDTO updateUserAnchor(String userId, String anchorId, UpdateUserAnchorRequest request) {
        Anchor anchor = getAnchorForUser(userId, anchorId);

        if (request.getTiktokId() != null) {
            String tiktokId = normalize(request.getTiktokId());
            if (tiktokId != null && !tiktokId.equals(anchor.getTiktokId())) {
                if (anchorRepository.findByUserIdAndTiktokId(userId, tiktokId).isPresent()) {
                    throw new BusinessException("主播已存在");
                }
                anchor.setTiktokId(tiktokId);
            } else if (tiktokId == null) {
                anchor.setTiktokId(null);
            }
        }

        if (request.getName() != null) {
            anchor.setName(request.getName());
        }

        if (request.getAvatarUrl() != null) {
            anchor.setAvatarUrl(normalize(request.getAvatarUrl()));
        }

        Anchor saved = anchorRepository.save(anchor);
        log.info("User anchor updated: {} for user: {}", saved.getId(), userId);
        return UserAnchorDTO.fromEntity(saved);
    }

    @Transactional
    public void deleteUserAnchor(String userId, String anchorId) {
        Anchor anchor = getAnchorForUser(userId, anchorId);
        if (presetAnchorRepository.countByAnchorId(anchorId) > 0) {
            throw new BusinessException("主播已被预设使用，无法删除");
        }

        anchorRepository.delete(anchor);
        log.info("User anchor deleted: {} for user: {}", anchorId, userId);
    }

    private Anchor getAnchorForUser(String userId, String anchorId) {
        Anchor anchor = anchorRepository.findById(anchorId)
                .orElseThrow(() -> new BusinessException("主播不存在"));
        if (!userId.equals(anchor.getUserId())) {
            throw new BusinessException("无权限访问该主播");
        }
        return anchor;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
