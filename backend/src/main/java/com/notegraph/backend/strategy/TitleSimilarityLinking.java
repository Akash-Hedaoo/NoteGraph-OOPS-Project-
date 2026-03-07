package com.notegraph.backend.strategy;

import com.notegraph.backend.model.Note;
import com.notegraph.backend.model.TextNote;

import java.util.ArrayList;
import java.util.List;

public class TitleSimilarityLinking implements LinkingStrategy {

    @Override
    public List<Note> suggestLinks(Note sourceNote, List<Note> allNotes) {
        List<Note> suggestions = new ArrayList<>();

        String content = "";
        if (sourceNote instanceof TextNote) {
            content = ((TextNote) sourceNote).getContent();
        }

        if (content == null || content.isEmpty()) {
            return suggestions;
        }

        String lowerContent = content.toLowerCase();

        for (Note targetNote : allNotes) {
            if (!targetNote.getId().equals(sourceNote.getId())) {
                String targetTitle = targetNote.getTitle();
                if (targetTitle != null && lowerContent.contains(targetTitle.toLowerCase())) {
                    suggestions.add(targetNote);
                }
            }
        }

        return suggestions;
    }
}
