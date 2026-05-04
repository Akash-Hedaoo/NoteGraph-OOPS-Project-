package com.notegraph.api.service;

import com.notegraph.api.domain.Note;
import com.notegraph.api.domain.Tag;
import com.notegraph.api.domain.User;
import com.notegraph.api.repository.NoteRepository;
import com.notegraph.api.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final TagRepository tagRepository;
    private final ActivityService activityService;

    public List<Note> getRecentNotesForWorkspace(UUID workspaceId) {
        return noteRepository.findByWorkspaceIdOrderByUpdatedAtDesc(workspaceId);
    }

    public List<Note> getNotesByTagId(UUID tagId) {
        return noteRepository.findByTagId(tagId);
    }

    public List<Note> getFavoritesForWorkspace(UUID workspaceId) {
        return noteRepository.findByWorkspaceIdAndFavoriteTrue(workspaceId);
    }

    public Note getNoteById(UUID noteId) {
        return noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found: " + noteId));
    }

    @Transactional
    public Note createNote(Note note) {
        Note savedNote = noteRepository.save(note);
        activityService.logActivity(savedNote.getOwner(), "created a new note", "NOTE", savedNote.getId(), savedNote.getTitle());
        return savedNote;
    }

    @Transactional
    public Note updateNote(UUID noteId, Note updatedDetails) {
        Note existingNote = getNoteById(noteId);
        existingNote.setTitle(updatedDetails.getTitle());
        existingNote.setContent(updatedDetails.getContent());
        if (updatedDetails.getStatus() != null) {
            existingNote.setStatus(updatedDetails.getStatus());
        }
        
        Note savedNode = noteRepository.save(existingNote);
        activityService.logActivity(existingNote.getOwner(), "edited", "NOTE", savedNode.getId(), savedNode.getTitle());
        return savedNode;
    }

    @Transactional
    public void toggleFavorite(UUID noteId) {
        Note note = getNoteById(noteId);
        note.setFavorite(!note.getFavorite());
        noteRepository.save(note);
    }

    @Transactional
    public Note addTagToNote(UUID noteId, UUID tagId) {
        Note note = getNoteById(noteId);
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId));
        
        note.getTags().add(tag);
        return noteRepository.save(note);
    }

    @Transactional
    public Note removeTagFromNote(UUID noteId, UUID tagId) {
        Note note = getNoteById(noteId);
        note.getTags().removeIf(t -> t.getId().equals(tagId));
        return noteRepository.save(note);
    }

    @Transactional
    public void deleteNote(UUID noteId) {
        Note note = getNoteById(noteId);
        // Capture references BEFORE deleting (lazy fields won't be accessible after delete)
        User owner = note.getOwner();
        String title = note.getTitle();
        UUID nId = note.getId();
        
        // Clear tag associations first to prevent FK constraint on note_tags join table
        note.getTags().clear();
        noteRepository.saveAndFlush(note);
        noteRepository.delete(note);
        
        activityService.logActivity(owner, "archived", "NOTE", nId, title);
    }
}
