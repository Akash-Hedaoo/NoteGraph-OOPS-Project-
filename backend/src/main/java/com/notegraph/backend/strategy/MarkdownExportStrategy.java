package com.notegraph.backend.strategy;

import com.notegraph.backend.model.Note;

public class MarkdownExportStrategy implements ExportStrategy {

    @Override
    public String export(Note note) {
        return "# " + note.getTitle() + "\n\n" + "Exported on: " + note.getUpdatedAt();
    }
}
