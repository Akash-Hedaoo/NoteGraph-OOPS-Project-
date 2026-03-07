package com.notegraph.backend.strategy;

import com.notegraph.backend.model.Note;

public class JsonExportStrategy implements ExportStrategy {

    @Override
    public String export(Note note) {
        return "{\n" +
                "  \"title\": \"" + note.getTitle() + "\"\n" +
                "}";
    }
}
