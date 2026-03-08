package com.notegraph.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceRequest {
    @NotBlank(message = "Workspace name cannot be empty")
    private String name;
}
