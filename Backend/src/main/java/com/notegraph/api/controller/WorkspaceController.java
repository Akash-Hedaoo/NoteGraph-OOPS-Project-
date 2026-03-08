package com.notegraph.api.controller;

import com.notegraph.api.dto.WorkspaceDto;
import com.notegraph.api.dto.WorkspaceRequest;
import com.notegraph.api.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @GetMapping
    public ResponseEntity<List<WorkspaceDto>> getUserWorkspaces(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(workspaceService.getUserWorkspaces(email));
    }

    @PostMapping
    public ResponseEntity<WorkspaceDto> createWorkspace(@Valid @RequestBody WorkspaceRequest request, Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(workspaceService.createWorkspace(email, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkspace(@PathVariable UUID id, Authentication authentication) {
        String email = authentication.getName();
        workspaceService.deleteWorkspace(email, id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkspaceDto> renameWorkspace(@PathVariable UUID id, @Valid @RequestBody WorkspaceRequest request, Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(workspaceService.renameWorkspace(email, id, request));
    }
}
