package com.notegraph.api.controller;

import com.notegraph.api.domain.RevisionSchedule;
import com.notegraph.api.service.RevisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/revisions")
@RequiredArgsConstructor
public class RevisionController {

    private final RevisionService revisionService;

    @GetMapping("/workspace/{wsId}/user/{userId}")
    public ResponseEntity<List<RevisionSchedule>> getWorkspaceRevisions(
            @PathVariable UUID wsId,
            @PathVariable UUID userId) {
        return ResponseEntity.ok(revisionService.getWorkspaceRevisions(wsId, userId));
    }

    @PostMapping
    public ResponseEntity<RevisionSchedule> scheduleRevision(@RequestBody RevisionService.RevisionRequest request) {
        try {
            RevisionSchedule revision = revisionService.scheduleRevision(request);
            return ResponseEntity.ok(revision);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<RevisionSchedule> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            return ResponseEntity.ok(revisionService.updateStatus(id, status));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRevision(@PathVariable UUID id) {
        revisionService.deleteRevision(id);
        return ResponseEntity.noContent().build();
    }
}
