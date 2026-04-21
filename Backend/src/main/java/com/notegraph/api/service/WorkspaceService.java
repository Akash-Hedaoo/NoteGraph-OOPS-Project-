package com.notegraph.api.service;

import com.notegraph.api.domain.Note;
import com.notegraph.api.domain.Tag;
import com.notegraph.api.domain.User;
import com.notegraph.api.domain.Workspace;
import com.notegraph.api.dto.WorkspaceDto;
import com.notegraph.api.dto.WorkspaceRequest;
import com.notegraph.api.repository.NoteRepository;
import com.notegraph.api.repository.TagRepository;
import com.notegraph.api.repository.UserRepository;
import com.notegraph.api.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final TagRepository tagRepository;

    public List<WorkspaceDto> getUserWorkspaces(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return workspaceRepository.findByOwnerId(user.getId())
                .stream()
                .map(WorkspaceDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkspaceDto createWorkspace(String email, WorkspaceRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                
        Workspace newWorkspace = Workspace.builder()
                .name(request.getName())
                .owner(user)
                .build();
                
        Workspace savedWorkspace = workspaceRepository.save(newWorkspace);
        return WorkspaceDto.fromEntity(savedWorkspace);
    }

    @Transactional
    public void deleteWorkspace(String email, UUID workspaceId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found: " + workspaceId));
                
        if (!workspace.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You do not have permission to delete this workspace.");
        }
        
        // 1. First clear all note-tag associations to prevent FK constraints
        List<Note> notes = noteRepository.findByWorkspaceIdOrderByUpdatedAtDesc(workspace.getId());
        for (Note note : notes) {
            note.getTags().clear();
        }
        noteRepository.saveAllAndFlush(notes);
        
        // 2. Delete all Notes in this workspace
        noteRepository.deleteAll(notes);
        noteRepository.flush();
        
        // 3. Delete all Tags in this workspace
        List<Tag> tags = tagRepository.findByWorkspaceId(workspace.getId());
        tagRepository.deleteAll(tags);
        tagRepository.flush();
        
        // 4. Finally delete the workspace itself
        workspaceRepository.delete(workspace);
    }

    @Transactional
    public WorkspaceDto renameWorkspace(String email, UUID workspaceId, WorkspaceRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found: " + workspaceId));

        if (!workspace.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You do not have permission to rename this workspace.");
        }

        workspace.setName(request.getName());
        Workspace savedWorkspace = workspaceRepository.save(workspace);
        return WorkspaceDto.fromEntity(savedWorkspace);
    }
}
