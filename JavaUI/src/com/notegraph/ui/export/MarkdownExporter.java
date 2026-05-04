package com.notegraph.ui.export;

import java.io.File;
import java.io.FileWriter;

/**
 * Concrete strategy for exporting notes as Markdown.
 */
public class MarkdownExporter implements NoteExporter {
    @Override
    public String getFormatName() {
        return "Markdown (.md)";
    }

    @Override
    public String getExtension() {
        return "md";
    }

    @Override
    public void export(String title, String content, File targetFile) throws Exception {
        // Convert basic HTML to Markdown
        String mdText = content;
        
        mdText = mdText.replaceAll("<p>", "").replaceAll("</p>", "\n\n");
        mdText = mdText.replaceAll("<b>(.*?)</b>", "**$1**");
        mdText = mdText.replaceAll("<strong>(.*?)</strong>", "**$1**");
        mdText = mdText.replaceAll("<i>(.*?)</i>", "*$1*");
        mdText = mdText.replaceAll("<em>(.*?)</em>", "*$1*");
        mdText = mdText.replaceAll("<u>(.*?)</u>", "__$1__");
        
        // Strip any remaining HTML
        mdText = mdText.replaceAll("<[^>]*>", "").replaceAll("&nbsp;", " ").replaceAll("&amp;", "&");

        try (FileWriter writer = new FileWriter(targetFile)) {
            writer.write("# " + title + "\n\n");
            writer.write(mdText.trim());
        }
    }
}
