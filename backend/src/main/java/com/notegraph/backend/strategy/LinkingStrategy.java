package com.notegraph.backend.strategy;

import com.notegraph.backend.model.Note;
import java.util.List;

public interface LinkingStrategy {
    List<Note> suggestLinks(Note sourceNote, List<Note> allNotes);
}
