package com.notegraph.ui.export;

import java.io.File;
import java.io.FileWriter;

/**
 * Concrete strategy for exporting notes as Plain Text.
 */
public class PlainTextExporter implements NoteExporter {
    @Override
    public String getFormatName() {
        return "Plain Text (.txt)";
    }

    @Override
    public String getExtension() {
        return "txt";
    }

    @Override
    public void export(String title, String content, File targetFile) throws Exception {
        // Strip HTML tags for plain text export
        String plainText = content.replaceAll("<p>", "").replaceAll("</p>", "\n");
        plainText = plainText.replaceAll("<[^>]*>", "").replaceAll("&nbsp;", " ").replaceAll("&amp;", "&");

        try (FileWriter writer = new FileWriter(targetFile)) {
            writer.write(title + "\n\n");
            writer.write(plainText);
        }
    }
}
