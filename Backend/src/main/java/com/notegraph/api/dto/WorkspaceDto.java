package com.notegraph.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.notegraph.api.domain.Workspace;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceDto {
    private UUID id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static WorkspaceDto fromEntity(Workspace workspace) {
        if (workspace == null) return null;
        return WorkspaceDto.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .build();
    }
}
