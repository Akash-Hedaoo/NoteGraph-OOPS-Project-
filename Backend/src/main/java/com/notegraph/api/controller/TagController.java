package com.notegraph.api.controller;

import com.notegraph.api.domain.Tag;
import com.notegraph.api.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<List<Tag>> getWorkspaceTags(@PathVariable UUID workspaceId) {
        return ResponseEntity.ok(tagService.getTagsForWorkspace(workspaceId));
    }

    @PostMapping
    public ResponseEntity<Tag> createTag(@RequestBody Tag tag) {
        return ResponseEntity.ok(tagService.createTag(tag));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
