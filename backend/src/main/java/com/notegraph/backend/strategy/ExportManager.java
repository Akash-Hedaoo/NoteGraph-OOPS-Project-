package com.notegraph.backend.strategy;

import com.notegraph.backend.model.Note;

public class ExportManager {

    private ExportStrategy strategy;

    public void setStrategy(ExportStrategy strategy) {
        this.strategy = strategy;
    }

    public String executeExport(Note note) {
        if (strategy != null) {
            return strategy.export(note);
        }
        throw new IllegalStateException("Strategy is not set");
    }
}
