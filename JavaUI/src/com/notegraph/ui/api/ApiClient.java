package com.notegraph.ui.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * REST API client for the Spring Boot backend.
 * Uses java.net.http.HttpClient and Gson for JSON.
 */
public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final Gson gson = new GsonBuilder().create();
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private String token;
    private ApiModels.UserDto currentUser;
    private String workspaceId;

    // ── Singleton ───────────────────────────────────
    private static ApiClient instance;
    public static ApiClient get() {
        if (instance == null) instance = new ApiClient();
        return instance;
    }
    private ApiClient() {}

    // ── Auth State ──────────────────────────────────
    public String getToken()       { return token; }
    public ApiModels.UserDto getUser() { return currentUser; }
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String id) { this.workspaceId = id; }
    public boolean isAuthenticated() { return token != null; }

    public void logout() {
        token = null;
        currentUser = null;
        workspaceId = null;
    }

    // ── Auth ────────────────────────────────────────
    public ApiModels.AuthResponse login(String email, String password) throws Exception {
        ApiModels.AuthRequest req = new ApiModels.AuthRequest(email, password);
        String body = post("/auth/login", gson.toJson(req));
        ApiModels.AuthResponse resp = gson.fromJson(body, ApiModels.AuthResponse.class);
        this.token = resp.token;
        this.currentUser = resp.user;
        this.workspaceId = resp.workspaceId;
        return resp;
    }

    public ApiModels.AuthResponse register(String email, String password, String name) throws Exception {
        ApiModels.RegisterRequest req = new ApiModels.RegisterRequest(email, password, name);
        String body = post("/auth/register", gson.toJson(req));
        ApiModels.AuthResponse resp = gson.fromJson(body, ApiModels.AuthResponse.class);
        this.token = resp.token;
        this.currentUser = resp.user;
        this.workspaceId = resp.workspaceId;
        return resp;
    }

    // ── Workspaces ──────────────────────────────────
    public List<ApiModels.WorkspaceDto> getWorkspaces() throws Exception {
        String body = get("/workspaces");
        return gson.fromJson(body, new TypeToken<List<ApiModels.WorkspaceDto>>(){}.getType());
    }

    public ApiModels.WorkspaceDto createWorkspace(String name) throws Exception {
        String body = post("/workspaces", gson.toJson(new ApiModels.WorkspaceRequest(name)));
        return gson.fromJson(body, ApiModels.WorkspaceDto.class);
    }

    public void deleteWorkspace(String id) throws Exception {
        delete("/workspaces/" + id);
    }

    public ApiModels.WorkspaceDto renameWorkspace(String id, String name) throws Exception {
        String body = put("/workspaces/" + id, gson.toJson(new ApiModels.WorkspaceRequest(name)));
        return gson.fromJson(body, ApiModels.WorkspaceDto.class);
    }

    // ── Notes ───────────────────────────────────────
    public List<ApiModels.Note> getWorkspaceNotes(String wsId) throws Exception {
        String body = get("/notes/workspace/" + wsId);
        return gson.fromJson(body, new TypeToken<List<ApiModels.Note>>(){}.getType());
    }

    public ApiModels.Note getNoteById(String id) throws Exception {
        String body = get("/notes/" + id);
        return gson.fromJson(body, ApiModels.Note.class);
    }

    public List<ApiModels.Note> getNotesByTag(String tagId) throws Exception {
        String body = get("/notes/tag/" + tagId);
        return gson.fromJson(body, new TypeToken<List<ApiModels.Note>>(){}.getType());
    }

    public ApiModels.Note createNote(ApiModels.Note note) throws Exception {
        String body = post("/notes", gson.toJson(note));
        return gson.fromJson(body, ApiModels.Note.class);
    }

    public ApiModels.Note updateNote(String id, ApiModels.Note note) throws Exception {
        String body = put("/notes/" + id, gson.toJson(note));
        return gson.fromJson(body, ApiModels.Note.class);
    }

    public void deleteNote(String id) throws Exception {
        delete("/notes/" + id);
    }

    public void toggleFavorite(String id) throws Exception {
        put("/notes/" + id + "/favorite", "");
    }

    public ApiModels.Note addTagToNote(String noteId, String tagId) throws Exception {
        String body = post("/notes/" + noteId + "/tags/" + tagId, "");
        return gson.fromJson(body, ApiModels.Note.class);
    }

    public ApiModels.Note removeTagFromNote(String noteId, String tagId) throws Exception {
        String body = delete("/notes/" + noteId + "/tags/" + tagId);
        return gson.fromJson(body, ApiModels.Note.class);
    }

    // ── Tags ────────────────────────────────────────
    public List<ApiModels.Tag> getWorkspaceTags(String wsId) throws Exception {
        String body = get("/tags/workspace/" + wsId);
        return gson.fromJson(body, new TypeToken<List<ApiModels.Tag>>(){}.getType());
    }

    public ApiModels.Tag createTag(ApiModels.Tag tag) throws Exception {
        String body = post("/tags", gson.toJson(tag));
        return gson.fromJson(body, ApiModels.Tag.class);
    }

    public ApiModels.Tag updateTag(String id, ApiModels.Tag tag) throws Exception {
        String body = put("/tags/" + id, gson.toJson(tag));
        return gson.fromJson(body, ApiModels.Tag.class);
    }

    public void deleteTag(String id) throws Exception {
        delete("/tags/" + id);
    }

    // ── Graph ───────────────────────────────────────
    public ApiModels.GraphResponse getGraph(String wsId) throws Exception {
        String body = get("/graph/" + wsId);
        return gson.fromJson(body, ApiModels.GraphResponse.class);
    }

    // ── Activity ────────────────────────────────────
    public List<ApiModels.ActivityLog> getUserActivity(String userId) throws Exception {
        String body = get("/activity/user/" + userId);
        return gson.fromJson(body, new TypeToken<List<ApiModels.ActivityLog>>(){}.getType());
    }

    // ── HTTP Helpers ────────────────────────────────
    private String get(String path) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .GET()
                .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        HttpResponse<String> resp = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        checkResponse(resp);
        return resp.body();
    }

    private String post(String path, String json) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .POST(HttpRequest.BodyPublishers.ofString(json != null ? json : ""))
                .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        HttpResponse<String> resp = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        checkResponse(resp);
        return resp.body();
    }

    private String put(String path, String json) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .PUT(HttpRequest.BodyPublishers.ofString(json != null ? json : ""))
                .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        HttpResponse<String> resp = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        checkResponse(resp);
        return resp.body();
    }

    private String delete(String path) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .DELETE()
                .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        HttpResponse<String> resp = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        checkResponse(resp);
        return resp.body() != null ? resp.body() : "";
    }

    private void checkResponse(HttpResponse<String> resp) throws Exception {
        int code = resp.statusCode();
        if (code >= 200 && code < 300) return;
        if (code == 401) {
            logout();
            throw new Exception("Session expired. Please login again.");
        }
        String msg = resp.body();
        if (msg != null && msg.length() > 200) msg = msg.substring(0, 200);
        throw new Exception("API Error " + code + ": " + msg);
    }
}
