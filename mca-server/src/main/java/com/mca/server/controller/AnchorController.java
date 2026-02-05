package com.mca.server.controller;

import com.mca.server.dto.ApiResponse;
import com.mca.server.dto.UserAnchorDTO;
import com.mca.server.dto.request.CreateUserAnchorRequest;
import com.mca.server.dto.request.UpdateUserAnchorRequest;
import com.mca.server.service.AnchorService;
import com.mca.server.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/anchors")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "主播库", description = "用户级主播管理接口")
@SecurityRequirement(name = "bearerAuth")
public class AnchorController {

    private final AnchorService anchorService;

    @Operation(summary = "获取主播列表", description = "获取当前用户的主播库列表")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserAnchorDTO>>> listAnchors() {
        String userId = SecurityUtil.getCurrentUserId();
        List<UserAnchorDTO> anchors = anchorService.listUserAnchors(userId);
        return ResponseEntity.ok(ApiResponse.success(anchors));
    }

    @Operation(summary = "创建主播", description = "创建新的用户主播")
    @PostMapping
    public ResponseEntity<ApiResponse<UserAnchorDTO>> createAnchor(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "创建主播请求", required = true)
            @RequestBody @Valid CreateUserAnchorRequest request) {
        String userId = SecurityUtil.getCurrentUserId();
        UserAnchorDTO anchor = anchorService.createUserAnchor(userId, request);
        return ResponseEntity.ok(ApiResponse.success("创建成功", anchor));
    }

    @Operation(summary = "更新主播", description = "更新用户主播信息")
    @PutMapping("/{anchorId}")
    public ResponseEntity<ApiResponse<UserAnchorDTO>> updateAnchor(
            @Parameter(description = "主播ID", required = true, example = "anchor_xyz789") @PathVariable String anchorId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "更新主播请求", required = true)
            @RequestBody @Valid UpdateUserAnchorRequest request) {
        String userId = SecurityUtil.getCurrentUserId();
        UserAnchorDTO anchor = anchorService.updateUserAnchor(userId, anchorId, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", anchor));
    }

    @Operation(summary = "删除主播", description = "删除用户主播")
    @DeleteMapping("/{anchorId}")
    public ResponseEntity<ApiResponse<Void>> deleteAnchor(
            @Parameter(description = "主播ID", required = true, example = "anchor_xyz789") @PathVariable String anchorId) {
        String userId = SecurityUtil.getCurrentUserId();
        anchorService.deleteUserAnchor(userId, anchorId);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
}
