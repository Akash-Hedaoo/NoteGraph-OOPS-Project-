package com.notegraph.api.service;

import com.notegraph.api.domain.Note;
import com.notegraph.api.domain.Tag;
import com.notegraph.api.repository.NoteRepository;
import com.notegraph.api.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final NoteRepository noteRepository;

    public List<Tag> getTagsForWorkspace(UUID workspaceId) {
        return tagRepository.findByWorkspaceId(workspaceId);
    }

    @Transactional
    public Tag createTag(Tag tag) {
        if (tagRepository.existsByWorkspaceIdAndNameIgnoreCase(tag.getWorkspace().getId(), tag.getName())) {
            throw new IllegalArgumentException("Tag with this name already exists in the workspace");
        }
        return tagRepository.save(tag);
    }

    @Transactional
    public void deleteTag(UUID tagId) {
        Tag tag = tagRepository.findById(tagId)
            .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId));
            
        // First disconnect this tag from all notes
        // Use removeIf with ID comparison (not remove(tag)) to avoid Hibernate proxy equality issues
        List<Note> notes = noteRepository.findByTagId(tagId);
        for (Note note : notes) {
            note.getTags().removeIf(t -> t.getId().equals(tagId));
        }
        noteRepository.saveAll(notes);
        noteRepository.flush();
        
        // Then safely delete the tag
        tagRepository.delete(tag);
    }

    @Transactional
    public Tag updateTag(UUID tagId, Tag updatedDetails) {
        Tag existingTag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId));
        
        if (!existingTag.getName().equalsIgnoreCase(updatedDetails.getName()) &&
            tagRepository.existsByWorkspaceIdAndNameIgnoreCase(existingTag.getWorkspace().getId(), updatedDetails.getName())) {
            throw new IllegalArgumentException("Tag with this name already exists in the workspace");
        }

        existingTag.setName(updatedDetails.getName());
        existingTag.setColor(updatedDetails.getColor());
        return tagRepository.save(existingTag);
    }
}
