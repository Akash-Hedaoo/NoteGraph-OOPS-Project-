package com.notegraph.ui.api;

import java.util.*;

/**
 * POJOs matching the Spring Boot backend's DTOs and domain models.
 * Uses simple public fields for Gson compatibility.
 */
public class ApiModels {

    // ── Auth ────────────────────────────────────────
    public static class AuthRequest {
        public String email;
        public String password;
        public AuthRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    public static class RegisterRequest {
        public String email;
        public String password;
        public String name;
        public RegisterRequest(String email, String password, String name) {
            this.email = email;
            this.password = password;
            this.name = name;
        }
    }

    public static class AuthResponse {
        public String token;
        public UserDto user;
        public String workspaceId;
    }

    public static class UserDto {
        public String id;
        public String name;
        public String email;
    }

    // ── Workspace ───────────────────────────────────
    public static class WorkspaceDto {
        public String id;
        public String name;
        public String ownerId;
    }

    public static class WorkspaceRequest {
        public String name;
        public WorkspaceRequest(String name) { this.name = name; }
    }

    // ── Note ────────────────────────────────────────
    public static class Note {
        public String id;
        public String title;
        public String content;
        public Boolean favorite;
        public String status;
        public String createdAt;
        public String updatedAt;
        public IdRef workspace;
        public IdRef owner;
        public List<Tag> tags;

        // Convenience for creating new notes
        public Note() { this.tags = new ArrayList<>(); }
    }

    // ── Tag ─────────────────────────────────────────
    public static class Tag {
        public String id;
        public String name;
        public String color;
        public IdRef workspace;
    }

    // ── Graph ───────────────────────────────────────
    public static class GraphResponse {
        public String rootTitle;
        public boolean isRoot;
        public String rootTagColor;
        public List<GraphBranch> branches;
    }

    public static class GraphBranch {
        public String tagId;
        public String tagTitle;
        public String tagColor;
        public int noteCount;
        public List<GraphLeaf> leaves;
    }

    public static class GraphLeaf {
        public String id;
        public String title;
        public String tagColor;
    }

    // ── Activity ────────────────────────────────────
    public static class ActivityLog {
        public String id;
        public String action;
        public String targetName;
        public String createdAt;
        public UserDto user;
    }

    // ── AI Assistant ────────────────────────────────
    public static class AiChatRequest {
        public String currentNoteId;
        public String currentNoteTitle;
        public String currentNoteContent;
        public List<String> currentTags;
        public List<AiChatMessage> history;
    }

    public static class AiChatMessage {
        public String role;
        public String text;
        public AiChatMessage() {}
        public AiChatMessage(String role, String text) {
            this.role = role;
            this.text = text;
        }
    }

    public static class AiChatResponse {
        public String answer;
        public String error;
    }

    // ── Helper ──────────────────────────────────────
    /** Simple id-only reference for workspace/owner fields. */
    public static class IdRef {
        public String id;
        public IdRef() {}
        public IdRef(String id) { this.id = id; }
    }
}
