package com.notegraph.frontend.strategy;

public class ExportContext {
    private ExportStrategy strategy;

    public ExportContext(ExportStrategy strategy) {
        this.strategy = strategy;
    }

    public String executeStrategy(String title, String content) {
        return strategy.format(title, content);
    }
}
