package com.notegraph.ui.export;

import java.io.File;
import java.io.FileWriter;

/**
 * Concrete strategy for exporting notes as HTML.
 */
public class HtmlExporter implements NoteExporter {
    @Override
    public String getFormatName() {
        return "HTML Document (.html)";
    }

    @Override
    public String getExtension() {
        return "html";
    }

    @Override
    public void export(String title, String content, File targetFile) throws Exception {
        String htmlWrap = "<!DOCTYPE html>\n" +
                          "<html>\n" +
                          "<head>\n" +
                          "  <meta charset=\"utf-8\">\n" +
                          "  <title>" + title + "</title>\n" +
                          "  <style>\n" +
                          "    body { font-family: sans-serif; line-height: 1.6; max-width: 800px; margin: 40px auto; padding: 20px; color: #333; }\n" +
                          "    h1 { color: #222; }\n" +
                          "  </style>\n" +
                          "</head>\n" +
                          "<body>\n" +
                          "  <h1>" + title + "</h1>\n" +
                          "  <hr/>\n" +
                          "  " + content + "\n" +
                          "</body>\n" +
                          "</html>";

        try (FileWriter writer = new FileWriter(targetFile)) {
            writer.write(htmlWrap);
        }
    }
}
