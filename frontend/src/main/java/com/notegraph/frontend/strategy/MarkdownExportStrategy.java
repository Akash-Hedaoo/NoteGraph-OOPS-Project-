package com.notegraph.frontend.strategy;

public class MarkdownExportStrategy implements ExportStrategy {
    @Override
    public String format(String title, String content) {
        return "# " + title + "\n\n" + content;
    }
}
