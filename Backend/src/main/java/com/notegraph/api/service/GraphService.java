package com.notegraph.api.service;

import com.notegraph.api.domain.Note;
import com.notegraph.api.domain.Tag;
import com.notegraph.api.repository.NoteRepository;
import com.notegraph.api.repository.TagRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GraphService {

    private final TagRepository tagRepository;
    private final NoteRepository noteRepository;

    /**
     * Constructs a hierarchical data payload explicitly designed for the visual
     * "Organizational Chart" component built in the React Frontend using Tags as parents.
     */
    public GraphHierarchyResponse getGraphHierarchy(UUID workspaceId) {
        List<Tag> tags = tagRepository.findByWorkspaceId(workspaceId);
        List<GraphBranch> branches = new ArrayList<>();

        for (Tag tag : tags) {
            List<Note> connectedNotes = noteRepository.findByTagId(tag.getId());
            
            // Only add branches if there are connected notes to visualize the tree
            if (!connectedNotes.isEmpty()) {
                
                List<GraphLeaf> leaves = connectedNotes.stream()
                        .map(n -> new GraphLeaf(n.getId(), n.getTitle(), tag.getColor()))
                        .toList();

                branches.add(new GraphBranch(
                        tag.getId(),
                        tag.getName(),
                        tag.getColor(),
                        connectedNotes.size(),
                        leaves
                ));
            }
        }

        return new GraphHierarchyResponse(
                "Global Workspace Data",
                true,
                "blue",
                branches
        );
    }

    // DTOs specifically for the front-end hierarchical tree format
    @Data
    @RequiredArgsConstructor
    public static class GraphHierarchyResponse {
        private final String rootTitle;
        private final boolean isRoot;
        private final String rootTagColor;
        private final List<GraphBranch> branches;
    }

    @Data
    @RequiredArgsConstructor
    public static class GraphBranch {
        private final UUID tagId;
        private final String tagTitle;
        private final String tagColor;
        private final int noteCount;
        private final List<GraphLeaf> leaves;
    }

    @Data
    @RequiredArgsConstructor
    public static class GraphLeaf {
        private final UUID id;
        private final String title;
        private final String tagColor; // Match parent's connection visually
    }
}
