package com.notegraph.backend.strategy;

import com.notegraph.backend.model.Note;
import com.notegraph.backend.model.TextNote;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KeywordBasedLinking implements LinkingStrategy {

    @Override
    public List<Note> suggestLinks(Note sourceNote, List<Note> allNotes) {
        List<Note> suggestions = new ArrayList<>();

        String sourceContent = "";
        if (sourceNote instanceof TextNote) {
            sourceContent = ((TextNote) sourceNote).getContent();
        }

        if (sourceContent == null || sourceContent.isEmpty()) {
            return suggestions;
        }

        Set<String> sourceWords = extractKeywords(sourceContent);

        for (Note targetNote : allNotes) {
            if (!targetNote.getId().equals(sourceNote.getId())) {
                String targetContent = "";
                if (targetNote instanceof TextNote) {
                    targetContent = ((TextNote) targetNote).getContent();
                }

                Set<String> targetWords = extractKeywords(targetContent);

                int sharedWords = 0;
                for (String word : sourceWords) {
                    if (targetWords.contains(word)) {
                        sharedWords++;
                    }
                }

                if (sharedWords >= 2) {
                    suggestions.add(targetNote);
                }
            }
        }

        return suggestions;
    }

    private Set<String> extractKeywords(String text) {
        Set<String> keywords = new HashSet<>();
        if (text == null)
            return keywords;

        String[] words = text.toLowerCase().split("\\W+");
        for (String word : words) {
            if (word.length() > 4) {
                keywords.add(word);
            }
        }
        return keywords;
    }
}
