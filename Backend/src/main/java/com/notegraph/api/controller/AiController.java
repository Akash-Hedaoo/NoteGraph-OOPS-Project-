package com.notegraph.api.controller;

import com.notegraph.api.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat/{workspaceId}")
    public ResponseEntity<Map<String, String>> chat(
            @PathVariable UUID workspaceId,
            @RequestBody AiService.AiChatRequest request) {
        try {
            String answer = aiService.generateChatResponse(workspaceId, request);
            return ResponseEntity.ok(Map.of("answer", answer));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to communicate with AI service: " + e.getMessage()));
        }
    }

    @PostMapping("/tags/suggest/{workspaceId}")
    public ResponseEntity<List<String>> suggestTags(
            @PathVariable UUID workspaceId,
            @RequestBody Map<String, String> request) {
        try {
            String content = request.getOrDefault("content", "");
            List<String> tags = aiService.suggestTags(workspaceId, content);
            return ResponseEntity.ok(tags);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/format")
    public ResponseEntity<Map<String, String>> formatContent(@RequestBody Map<String, String> request) {
        try {
            String content = request.getOrDefault("content", "");
            String formatted = aiService.formatContent(content);
            return ResponseEntity.ok(Map.of("formatted", formatted));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to communicate with AI service: " + e.getMessage()));
        }
    }

    @PostMapping("/revision-plan/{workspaceId}")
    public ResponseEntity<?> generateRevisionPlan(@PathVariable UUID workspaceId) {
        try {
            var plan = aiService.generateRevisionPlan(workspaceId);
            return ResponseEntity.ok(plan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to generate revision plan: " + e.getMessage()));
        }
    }
}
