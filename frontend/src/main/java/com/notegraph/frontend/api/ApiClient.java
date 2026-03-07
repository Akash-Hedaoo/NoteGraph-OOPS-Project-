package com.notegraph.frontend.api;

import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;

public class ApiClient {
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new Gson();

    public static Long saveTextNote(Long noteId, String title, String content, String tags) {
        try {
            com.google.gson.JsonObject jsonObject = new com.google.gson.JsonObject();
            jsonObject.addProperty("title", title);
            jsonObject.addProperty("content", content);
            jsonObject.addProperty("tags", tags);
            jsonObject.addProperty("noteType", "TEXT");

            String requestBody = jsonObject.toString();

            HttpRequest request;
            if (noteId == null) {
                request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/notes/text"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + AuthClient.jwtToken)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
            } else {
                request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/notes/" + noteId))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + AuthClient.jwtToken)
                        .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
            }

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode == 200 || statusCode == 201) {
                com.google.gson.JsonObject responseObj = com.google.gson.JsonParser.parseString(response.body())
                        .getAsJsonObject();
                if (responseObj.has("id")) {
                    return responseObj.get("id").getAsLong();
                }
                return null;
            } else {
                System.err.println("SAVE FAILED: " + response.statusCode() + " - " + response.body());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean saveCanvasNote(String title, String content) {
        try {
            com.google.gson.JsonObject jsonObject = new com.google.gson.JsonObject();
            jsonObject.addProperty("title", title);
            jsonObject.addProperty("content", content);
            jsonObject.addProperty("noteType", "CANVAS");

            String requestBody = jsonObject.toString();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/notes/canvas"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + AuthClient.jwtToken)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode == 200 || statusCode == 201) {
                return true;
            } else {
                System.out.println("Error Body: " + response.body());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static com.notegraph.frontend.model.NoteDto fetchNoteById(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/notes/" + id))
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + AuthClient.jwtToken)
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return GSON.fromJson(response.body(), com.notegraph.frontend.model.NoteDto.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<com.notegraph.frontend.model.NoteDto> fetchAllNotes() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/notes"))
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + AuthClient.jwtToken)
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                return GSON.fromJson(
                        responseBody,
                        new com.google.gson.reflect.TypeToken<List<com.notegraph.frontend.model.NoteDto>>() {
                        }.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static List<com.notegraph.frontend.model.ConnectionDto> fetchAllConnections() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/notes/connections"))
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + AuthClient.jwtToken)
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                return GSON.fromJson(
                        responseBody,
                        new com.google.gson.reflect.TypeToken<List<com.notegraph.frontend.model.ConnectionDto>>() {
                        }.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static List<com.notegraph.frontend.model.NoteDto> getSuggestions(Long noteId, String strategy) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/notes/" + noteId + "/suggestions?strategy=" + strategy))
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + AuthClient.jwtToken)
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                return GSON.fromJson(
                        responseBody,
                        new com.google.gson.reflect.TypeToken<List<com.notegraph.frontend.model.NoteDto>>() {
                        }.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static boolean createConnection(Long sourceId, Long targetId) {
        try {
            com.google.gson.JsonObject jsonObject = new com.google.gson.JsonObject();
            jsonObject.addProperty("sourceId", sourceId);
            jsonObject.addProperty("targetId", targetId);

            String requestBody = jsonObject.toString();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/notes/connections"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + AuthClient.jwtToken)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode == 200 || statusCode == 201) {
                return true;
            } else {
                System.out.println("Error Body: " + response.body());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteNote(Long noteId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/notes/" + noteId))
                    .header("Authorization", "Bearer " + AuthClient.jwtToken)
                    .DELETE()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 || response.statusCode() == 204;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteVersion(Long noteId, Long versionId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/notes/" + noteId + "/versions/" + versionId))
                    .header("Authorization", "Bearer " + AuthClient.jwtToken)
                    .DELETE()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 || response.statusCode() == 204;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
