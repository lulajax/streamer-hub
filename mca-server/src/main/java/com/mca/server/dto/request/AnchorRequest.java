package com.mca.server.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnchorRequest {

    private String id;

    private String tiktokId;

    private String name;

    private String avatarUrl;

    private List<String> exclusiveGifts;

    private Integer displayOrder;
}
