package com.notegraph.backend.controller;

import com.notegraph.backend.service.SuggestionService;
import com.notegraph.backend.dto.ConnectionDto;
import com.notegraph.backend.model.Connection;
import com.notegraph.backend.model.Note;
import com.notegraph.backend.model.TextNote;
import com.notegraph.backend.repository.ConnectionRepository;
import com.notegraph.backend.repository.NoteRepository;
import com.notegraph.backend.dto.NoteDto;
import com.notegraph.backend.model.Tag;
import com.notegraph.backend.model.User;
import com.notegraph.backend.memento.NoteVersion;
import com.notegraph.backend.memento.NoteVersionRepository;
import com.notegraph.backend.repository.TagRepository;
import com.notegraph.backend.repository.UserRepository;
import com.notegraph.backend.dto.NoteVersionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private ConnectionRepository connectionRepository;

    @Autowired
    private NoteVersionRepository noteVersionRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow();
    }

    @PostMapping("/text")
    public TextNote createTextNote(@RequestBody java.util.Map<String, String> payload) {
        User currentUser = getCurrentUser();
        String title = payload.get("title");
        String content = payload.get("content");
        String tagsText = payload.get("tags");

        TextNote textNote = new TextNote();
        textNote.setUser(currentUser);
        textNote.setTitle(title);
        textNote.setContent(content);

        if (tagsText != null && !tagsText.trim().isEmpty()) {
            textNote.getTags().clear();
            String[] tagsArray = tagsText.split(",");
            for (String rawTagName : tagsArray) {
                String tagName = rawTagName.trim();
                if (!tagName.isEmpty()) {
                    Tag tag = tagRepository.findByName(tagName).orElseGet(() -> {
                        Tag t = new Tag();
                        t.setName(tagName);
                        return tagRepository.save(t);
                    });
                    textNote.getTags().add(tag);
                }
            }
        }

        TextNote savedNote = noteRepository.save(textNote);

        NoteVersion version = new NoteVersion();
        version.setNote(savedNote);
        version.setContent(content);
        version.setSavedAt(LocalDateTime.now());
        noteVersionRepository.save(version);

        if (content != null) {
            Pattern pattern = Pattern.compile("\\[\\[(.*?)\\]\\]");
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                String linkedTitle = matcher.group(1);
                Optional<Note> targetNoteOpt = noteRepository.findByTitle(linkedTitle);
                if (targetNoteOpt.isPresent()) {
                    Connection connection = new Connection();
                    connection.setSourceNote(savedNote);
                    connection.setTargetNote(targetNoteOpt.get());
                    connection.setRelationshipType("wikilink");
                    connectionRepository.save(connection);
                }
            }
        }

        return savedNote;
    }

    @GetMapping
    public List<Note> getAllNotes() {
        User currentUser = getCurrentUser();
        return noteRepository.findByUserAndIsTrashedFalse(currentUser);
    }

    @GetMapping("/trash")
    public List<Note> getTrash() {
        User currentUser = getCurrentUser();
        return noteRepository.findByUserAndIsTrashedTrue(currentUser);
    }

    @GetMapping("/connections")
    public List<ConnectionDto> getConnections() {
        return connectionRepository.findAll().stream().map(connection -> {
            ConnectionDto dto = new ConnectionDto();
            dto.setSourceId(connection.getSourceNote().getId());
            dto.setTargetId(connection.getTargetNote().getId());
            return dto;
        }).collect(Collectors.toList());
    }

    @Autowired
    private SuggestionService suggestionService;

    @GetMapping("/{id}/suggestions")
    public List<NoteDto> getSuggestions(@PathVariable("id") Long id, @RequestParam String strategy) {
        return suggestionService.getSuggestions(id, strategy).stream()
                .map(NoteDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/versions")
    public List<NoteVersionDto> getVersions(@PathVariable("id") Long id) {
        return noteVersionRepository.findByNoteIdOrderBySavedAtDesc(id).stream()
                .map(version -> {
                    NoteVersionDto dto = new NoteVersionDto();
                    dto.setId(version.getId());
                    dto.setContent(version.getContent());
                    dto.setSavedAt(version.getSavedAt());
                    return dto;
                }).collect(Collectors.toList());
    }

    @PostMapping("/connections")
    public ResponseEntity<?> createConnection(@RequestBody ConnectionDto dto) {
        Optional<Note> sourceOpt = noteRepository.findById(dto.getSourceId());
        Optional<Note> targetOpt = noteRepository.findById(dto.getTargetId());
        if (sourceOpt.isPresent() && targetOpt.isPresent()) {
            Connection connection = new Connection();
            connection.setSourceNote(sourceOpt.get());
            connection.setTargetNote(targetOpt.get());
            connection.setRelationshipType("approved-suggestion");
            connectionRepository.save(connection);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("Invalid source or target note ID");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable("id") Long id) {
        User currentUser = getCurrentUser();
        Optional<Note> noteOpt = noteRepository.findById(id);
        if (noteOpt.isPresent()) {
            Note note = noteOpt.get();
            if (!note.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).build();
            }
            note.setTrashed(true);
            noteRepository.save(note);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<?> restoreNote(@PathVariable("id") Long id) {
        User currentUser = getCurrentUser();
        Optional<Note> noteOpt = noteRepository.findById(id);
        if (noteOpt.isPresent()) {
            Note note = noteOpt.get();
            if (!note.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).build();
            }
            note.setTrashed(false);
            noteRepository.save(note);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<?> permanentDeleteNote(@PathVariable("id") Long id) {
        User currentUser = getCurrentUser();
        Optional<Note> noteOpt = noteRepository.findById(id);
        if (noteOpt.isPresent()) {
            Note note = noteOpt.get();
            if (!note.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).build();
            }
            noteRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{noteId}/versions/{versionId}")
    public ResponseEntity<?> deleteVersion(@PathVariable("noteId") Long noteId,
            @PathVariable("versionId") Long versionId) {
        noteVersionRepository.deleteById(versionId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNote(@PathVariable("id") Long id,
            @RequestBody java.util.Map<String, String> payload) {
        User currentUser = getCurrentUser();
        String title = payload.get("title");
        String content = payload.get("content");
        String tagsText = payload.get("tags");

        Note existingNote = noteRepository.findById(id).orElseThrow();
        if (!existingNote.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        existingNote.setTitle(title);
        if (existingNote instanceof TextNote) {
            ((TextNote) existingNote).setContent(content);
        }

        if (tagsText != null && !tagsText.trim().isEmpty()) {
            existingNote.getTags().clear();
            String[] tagsArray = tagsText.split(",");
            for (String rawTagName : tagsArray) {
                String tagName = rawTagName.trim();
                if (!tagName.isEmpty()) {
                    Tag tag = tagRepository.findByName(tagName).orElseGet(() -> {
                        Tag t = new Tag();
                        t.setName(tagName);
                        return tagRepository.save(t);
                    });
                    existingNote.getTags().add(tag);
                }
            }
        }

        Note savedNote = noteRepository.save(existingNote);

        NoteVersion version = new NoteVersion();
        version.setNote(savedNote);
        version.setContent(content);
        version.setSavedAt(LocalDateTime.now());
        noteVersionRepository.save(version);

        return ResponseEntity.ok(savedNote);
    }
}
