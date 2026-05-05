package com.notegraph.api.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.notegraph.api.domain.Note;
import com.notegraph.api.domain.Tag;
import com.notegraph.api.repository.NoteRepository;
import com.notegraph.api.repository.TagRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AiService {

    @Value("${mistral.api.key:}")
    private String mistralApiKey;

    private final NoteRepository noteRepository;
    private final TagRepository tagRepository;
    private final Gson gson = new Gson();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String generateChatResponse(UUID workspaceId, AiChatRequest request) throws Exception {
        Set<Note> relatedNotes = new HashSet<>();
        if (request.getCurrentTags() != null) {
            for (String tagIdStr : request.getCurrentTags()) {
                try {
                    UUID tagId = UUID.fromString(tagIdStr);
                    relatedNotes.addAll(noteRepository.findByTagId(tagId));
                } catch (Exception ignored) {}
            }
        }

        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("You are an AI assistant in a knowledge graph note-taking app. ");
        contextBuilder.append("You help the user write notes and answer questions based on their connected knowledge.\n\n");
        contextBuilder.append("--- CURRENT NOTE CONTEXT ---\n");
        contextBuilder.append("Title: ").append(request.getCurrentNoteTitle() != null ? request.getCurrentNoteTitle() : "Untitled").append("\n");
        contextBuilder.append("Content:\n").append(request.getCurrentNoteContent() != null ? request.getCurrentNoteContent() : "").append("\n\n");

        if (!relatedNotes.isEmpty()) {
            contextBuilder.append("--- RELATED NOTES (KNOWLEDGE GRAPH) ---\n");
            for (Note n : relatedNotes) {
                if (request.getCurrentNoteId() == null || !n.getId().toString().equals(request.getCurrentNoteId())) {
                    contextBuilder.append("Title: ").append(n.getTitle()).append("\n");
                    contextBuilder.append("Content:\n").append(n.getContent() != null ? n.getContent().replaceAll("<[^>]*>", " ") : "").append("\n\n");
                }
            }
        }
        contextBuilder.append("----------------------------\n");

        // Build Mistral messages array
        JsonArray messages = new JsonArray();

        // System message with context
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", contextBuilder.toString());
        messages.add(systemMessage);

        // Add conversation history
        if (request.getHistory() != null) {
            for (AiChatMessage msg : request.getHistory()) {
                JsonObject msgNode = new JsonObject();
                // Mistral uses "assistant" instead of Gemini's "model"
                String role = "model".equals(msg.getRole()) ? "assistant" : msg.getRole();
                msgNode.addProperty("role", role);
                msgNode.addProperty("content", msg.getText());
                messages.add(msgNode);
            }
        }

        JsonObject requestBody = new JsonObject();
        requestBody.add("messages", messages);

        return callMistral(requestBody);
    }

    public List<String> suggestTags(UUID workspaceId, String noteContent) throws Exception {
        List<Tag> existingTags = tagRepository.findByWorkspaceId(workspaceId);
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a tagging assistant. Based on the following note content, suggest 3-5 tags.\n");
        prompt.append("Here are the existing tags in the workspace: ");
        if (existingTags.isEmpty()) {
            prompt.append("None.\n");
        } else {
            List<String> names = existingTags.stream().map(Tag::getName).toList();
            prompt.append(String.join(", ", names)).append(".\n");
        }
        prompt.append("Prefer reusing existing tags if they fit perfectly. Otherwise, suggest new relevant ones.\n");
        prompt.append("Format your response as a strict JSON array of strings, e.g. [\"Development\", \"Architecture\"]. Do not include markdown formatting.\n\n");
        prompt.append("Note Content:\n").append(noteContent);

        JsonArray messages = new JsonArray();
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt.toString());
        messages.add(userMessage);

        JsonObject requestBody = new JsonObject();
        requestBody.add("messages", messages);
        requestBody.addProperty("response_format", "json_object");

        String responseText = callMistral(requestBody);
        
        try {
            JsonArray arrayNode = JsonParser.parseString(responseText).getAsJsonArray();
            List<String> tags = new ArrayList<>();
            for (int i = 0; i < arrayNode.size(); i++) {
                tags.add(arrayNode.get(i).getAsString().trim().replaceAll("[^a-zA-Z0-9- ]", "")); 
            }
            return tags;
        } catch (Exception e) {
            // Fallback: try to extract JSON array from response
            try {
                String trimmed = responseText.trim();
                int start = trimmed.indexOf('[');
                int end = trimmed.lastIndexOf(']');
                if (start >= 0 && end > start) {
                    String jsonArray = trimmed.substring(start, end + 1);
                    JsonArray arr = JsonParser.parseString(jsonArray).getAsJsonArray();
                    List<String> tags = new ArrayList<>();
                    for (int i = 0; i < arr.size(); i++) {
                        tags.add(arr.get(i).getAsString().trim().replaceAll("[^a-zA-Z0-9- ]", ""));
                    }
                    return tags;
                }
            } catch (Exception ignored) {}
        }
        return Collections.emptyList();
    }

    public String formatContent(String noteContent) throws Exception {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a text formatting assistant. Please format the following messy note content into a clean, well-structured, easy to read plain text document.\n");
        prompt.append("Fix spelling and grammatical errors, add proper spacing, newlines, and bullet points if necessary.\n");
        prompt.append("IMPORTANT: Return ONLY the formatted text. DO NOT use Markdown asterisks or hash tags (like **bold** or # Heading), just use standard plain text formatting with newlines and bullet characters like '•'.\n\n");
        prompt.append("Note Content:\n").append(noteContent);

        JsonArray messages = new JsonArray();
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt.toString());
        messages.add(userMessage);

        JsonObject requestBody = new JsonObject();
        requestBody.add("messages", messages);

        return callMistral(requestBody);
    }

    /**
     * Generate a revision plan for all notes in a workspace using Ebbinghaus forgetting curve.
     */
    public List<RevisionPlanItem> generateRevisionPlan(UUID workspaceId) throws Exception {
        List<Note> notes = noteRepository.findByWorkspaceIdOrderByUpdatedAtDesc(workspaceId);
        if (notes.isEmpty()) {
            return Collections.emptyList();
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an intelligent revision planning assistant. Analyze the following notes and create a spaced-repetition revision plan.\n\n");
        prompt.append("IMPORTANT RULES:\n");
        prompt.append("1. Assess the COMPLEXITY of each note on a scale of 1-10 based on technical depth, concepts involved, and difficulty.\n");
        prompt.append("2. Apply the EBBINGHAUS FORGETTING CURVE: humans forget ~56% within 1 hour, ~66% within 1 day, ~75% within 6 days.\n");
        prompt.append("3. Consider the TIME ELAPSED since note creation — older unreviewed notes are MORE URGENT.\n");
        prompt.append("4. Higher complexity notes need MORE FREQUENT revision intervals.\n");
        prompt.append("5. Suggest 2-3 revision dates per note based on optimal spaced-repetition intervals.\n");
        prompt.append("6. Rate urgency as HIGH (overdue), MEDIUM (due soon), or LOW (well within schedule).\n\n");
        prompt.append("Current date/time: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n\n");
        prompt.append("--- NOTES ---\n");

        for (Note note : notes) {
            prompt.append("Note ID: ").append(note.getId().toString()).append("\n");
            prompt.append("Title: ").append(note.getTitle()).append("\n");
            String content = note.getContent() != null ? note.getContent().replaceAll("<[^>]*>", " ") : "";
            if (content.length() > 500) content = content.substring(0, 500);
            prompt.append("Content Preview: ").append(content).append("\n");
            prompt.append("Created At: ").append(note.getCreatedAt() != null ? note.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "unknown").append("\n");
            prompt.append("Last Updated: ").append(note.getUpdatedAt() != null ? note.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "unknown").append("\n");
            long daysSinceCreation = note.getCreatedAt() != null ? ChronoUnit.DAYS.between(note.getCreatedAt(), LocalDateTime.now()) : 0;
            prompt.append("Days Since Creation: ").append(daysSinceCreation).append("\n");
            prompt.append("---\n");
        }

        prompt.append("\nReturn a JSON array with this exact structure for EACH note:\n");
        prompt.append("[{\"noteId\":\"uuid\", \"title\":\"string\", \"complexity\":number(1-10), \"urgency\":\"HIGH|MEDIUM|LOW\", ");
        prompt.append("\"suggestedDates\":[\"ISO-8601 datetime strings\"], \"reason\":\"explanation of why this revision schedule is optimal\"}]\n");
        prompt.append("\nDo not include any markdown formatting. Return ONLY the JSON array.");

        JsonArray messages = new JsonArray();
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt.toString());
        messages.add(userMessage);

        JsonObject requestBody = new JsonObject();
        requestBody.add("messages", messages);

        String responseText = callMistral(requestBody);

        try {
            // Try to parse directly as JSON array
            String trimmed = responseText.trim();
            if (!trimmed.startsWith("[")) {
                // Extract JSON array if wrapped in other text
                int start = trimmed.indexOf('[');
                int end = trimmed.lastIndexOf(']');
                if (start >= 0 && end > start) {
                    trimmed = trimmed.substring(start, end + 1);
                }
            }
            
            JsonArray arrayNode = JsonParser.parseString(trimmed).getAsJsonArray();
            List<RevisionPlanItem> plan = new ArrayList<>();
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonObject obj = arrayNode.get(i).getAsJsonObject();
                RevisionPlanItem item = new RevisionPlanItem();
                item.setNoteId(obj.has("noteId") ? obj.get("noteId").getAsString() : "");
                item.setTitle(obj.has("title") ? obj.get("title").getAsString() : "");
                item.setComplexity(obj.has("complexity") ? obj.get("complexity").getAsInt() : 5);
                item.setUrgency(obj.has("urgency") ? obj.get("urgency").getAsString() : "MEDIUM");
                item.setReason(obj.has("reason") ? obj.get("reason").getAsString() : "");
                List<String> dates = new ArrayList<>();
                if (obj.has("suggestedDates")) {
                    JsonArray datesArr = obj.getAsJsonArray("suggestedDates");
                    for (int j = 0; j < datesArr.size(); j++) {
                        dates.add(datesArr.get(j).getAsString());
                    }
                }
                item.setSuggestedDates(dates);
                plan.add(item);
            }
            return plan;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to parse AI revision plan response: " + responseText);
        }
    }

    private String callMistral(JsonObject requestBody) throws Exception {
        if (mistralApiKey == null || mistralApiKey.trim().isEmpty() || "YOUR_API_KEY_HERE".equals(mistralApiKey)) {
            throw new IllegalArgumentException("Mistral API key is not configured. Please add MISTRAL_API_KEY to your environment variables or application.yml.");
        }

        // Dynamic Runtime Fallback List of Mistral Models
        String[] fallbackModels = {
            "mistral-small-latest",
            "mistral-large-latest",
            "open-mistral-nemo"
        };

        for (int i = 0; i < fallbackModels.length; i++) {
            String model = fallbackModels[i];
            
            // Set the model in the request body
            requestBody.addProperty("model", model);

            String jsonBody = gson.toJson(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mistral.ai/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + mistralApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                // 503 (Unavailable) or 429 (Too Many Requests) -> Instantly fallback to the next model in the ring
                if ((response.statusCode() == 503 || response.statusCode() == 429) && i < fallbackModels.length - 1) {
                    continue;
                }
                
                if (response.statusCode() >= 400) {
                    // Do not fail entirely if one model breaks, try the next!
                    if (i < fallbackModels.length - 1) continue;
                    throw new RuntimeException("Mistral API error (" + response.statusCode() + "): " + response.body());
                }

                JsonObject responseNode = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonArray choices = responseNode.getAsJsonArray("choices");
                if (choices != null && choices.size() > 0) {
                    JsonObject message = choices.get(0).getAsJsonObject()
                            .getAsJsonObject("message");
                    if (message != null && message.has("content")) {
                        return message.get("content").getAsString();
                    }
                }
            } catch (Exception e) {
                if (i == fallbackModels.length - 1) {
                    return "Sorry, all our AI models are currently experiencing high burst limits. Please try again in 10 seconds.";
                }
            }
        }
        return "Sorry, the AI model array is currently unreachable.";
    }

    // DTOs
    @Data
    public static class AiChatRequest {
        private String currentNoteId;
        private String currentNoteTitle;
        private String currentNoteContent;
        private List<String> currentTags;
        private List<AiChatMessage> history;
    }

    @Data
    public static class AiChatMessage {
        private String role; // "user" or "model"
        private String text;
    }

    @Data
    public static class RevisionPlanItem {
        private String noteId;
        private String title;
        private int complexity;
        private String urgency;
        private List<String> suggestedDates;
        private String reason;
    }
}
