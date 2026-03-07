package com.notegraph.backend.strategy;

import com.notegraph.backend.model.Note;

public interface ExportStrategy {
    String export(Note note);
}
