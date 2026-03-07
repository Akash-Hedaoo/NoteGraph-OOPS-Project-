package com.notegraph.backend.service;

import com.notegraph.backend.model.Note;
import com.notegraph.backend.repository.NoteRepository;
import com.notegraph.backend.strategy.KeywordBasedLinking;
import com.notegraph.backend.strategy.LinkingStrategy;
import com.notegraph.backend.strategy.TitleSimilarityLinking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SuggestionService {

    @Autowired
    private NoteRepository noteRepository;

    public List<Note> getSuggestions(Long noteId, String strategyType) {
        Optional<Note> sourceNoteOpt = noteRepository.findById(noteId);
        if (sourceNoteOpt.isEmpty()) {
            return new ArrayList<>();
        }

        Note sourceNote = sourceNoteOpt.get();
        List<Note> allNotes = noteRepository.findAll();

        LinkingStrategy strategy;
        if (strategyType != null && strategyType.equalsIgnoreCase("KEYWORD")) {
            strategy = new KeywordBasedLinking();
        } else {
            strategy = new TitleSimilarityLinking();
        }

        return strategy.suggestLinks(sourceNote, allNotes);
    }
}
