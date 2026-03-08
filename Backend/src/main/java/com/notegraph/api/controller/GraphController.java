package com.notegraph.api.controller;

import com.notegraph.api.service.GraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/graph")
@RequiredArgsConstructor
public class GraphController {

    private final GraphService graphService;

    @GetMapping("/{workspaceId}")
    public ResponseEntity<GraphService.GraphHierarchyResponse> getGraphData(@PathVariable UUID workspaceId) {
        return ResponseEntity.ok(graphService.getGraphHierarchy(workspaceId));
    }
}
