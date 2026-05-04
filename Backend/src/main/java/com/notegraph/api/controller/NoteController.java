package com.notegraph.api.controller;

import com.notegraph.api.domain.Note;
import com.notegraph.api.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.badRequest().body(e.getMessage() + " | " + (e.getCause() != null ? e.getCause().getMessage() : ""));
    }

    private final NoteService noteService;

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<List<Note>> getWorkspaceNotes(@PathVariable UUID workspaceId) {
        return ResponseEntity.ok(noteService.getRecentNotesForWorkspace(workspaceId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable UUID id) {
        return ResponseEntity.ok(noteService.getNoteById(id));
    }

    @GetMapping("/tag/{tagId}")
    public ResponseEntity<List<Note>> getNotesByTag(@PathVariable UUID tagId) {
        return ResponseEntity.ok(noteService.getNotesByTagId(tagId));
    }

    @PostMapping
    public ResponseEntity<Note> createNote(@RequestBody Note note) {
        return ResponseEntity.ok(noteService.createNote(note));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable UUID id, @RequestBody Note note) {
        return ResponseEntity.ok(noteService.updateNote(id, note));
    }

    @PutMapping("/{id}/favorite")
    public ResponseEntity<Void> toggleFavorite(@PathVariable UUID id) {
        noteService.toggleFavorite(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{noteId}/tags/{tagId}")
    public ResponseEntity<Note> addTagToNote(@PathVariable UUID noteId, @PathVariable UUID tagId) {
        return ResponseEntity.ok(noteService.addTagToNote(noteId, tagId));
    }

    @DeleteMapping("/{noteId}/tags/{tagId}")
    public ResponseEntity<Note> removeTagFromNote(@PathVariable UUID noteId, @PathVariable UUID tagId) {
        return ResponseEntity.ok(noteService.removeTagFromNote(noteId, tagId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable UUID id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }
}
